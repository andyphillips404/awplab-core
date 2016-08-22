package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.service.codec.PojoCodec;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanUtil;
import org.bson.codecs.DocumentCodec;
import org.bson.conversions.Bson;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.expiry.Expirations;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by andyphillips404 on 8/16/16.
 */
public class MongoCollectionContainer<T> extends AbstractContainer implements Container.Sortable, Container.Indexed, AutoCloseable {

    final private MongoCollection<T> mongoCollection;

    private Bson filter;

    private Bson sort = Sorts.ascending("_id");

    private final List<PropertyDescriptor> propertyDescriptors;
    private final List<String> names;

    private final PojoCodec<T> pojoCodec;

    private final Class<T> type;

    private UserManagedCache<Integer, T> itemCache;

    public MongoCollectionContainer(MongoCollection<T> mongoCollection, Bson filter) throws IntrospectionException {
        this.mongoCollection = mongoCollection;
        this.type = mongoCollection.getDocumentClass();
        this.filter = filter;

        this.propertyDescriptors = BeanUtil.getBeanPropertyDescriptor(type);
        this.names = this.propertyDescriptors.stream().map(FeatureDescriptor::getName).collect(Collectors.toList());
        this.pojoCodec = new PojoCodec<>(type, new DocumentCodec());

        rebuildCache();

        updateCount(true);


    }

    private void rebuildCache() {
        if (itemCache != null) {
            itemCache.clear();
            itemCache.close();
        }

        itemCache = UserManagedCacheBuilder.newUserManagedCacheBuilder(Integer.class, type)
                .withExpiry(Expirations.<Integer, T>timeToIdleExpiration(new org.ehcache.expiry.Duration(cacheTimeToIdle.toMillis(), TimeUnit.MILLISECONDS)))
                .build(true);
    }

    private int cachePageSizeLookAhead = 20;

    private Duration cacheTimeToIdle = Duration.ofSeconds(30);

    private Duration timeBetweenSizeUpdateChecks = Duration.ofSeconds(1);

    private int count = -1;
    private Date countLastUpdated = new Date(0);

    private void updateCount(boolean force) {
        if (force || !Duration.between(countLastUpdated.toInstant(), new Date().toInstant()).minus(timeBetweenSizeUpdateChecks).isNegative()) {

            int newCount = filter != null ? (int)mongoCollection.count(filter) : (int)mongoCollection.count();
            if (newCount != count) {
                count = newCount;
                if (!force) fireItemSetChange();
            }

            countLastUpdated = new Date();
        }
    }


    public int getCachePageSizeLookAhead() {
        return cachePageSizeLookAhead;
    }

    public void setCachePageSizeLookAhead(int cachePageSizeLookAhead) {
        this.cachePageSizeLookAhead = cachePageSizeLookAhead;

    }

    public Duration getCacheTimeToIdle() {
        return cacheTimeToIdle;
    }

    public void setTimeToIdleSeconds(Duration timeToIdle) {
        this.cacheTimeToIdle = timeToIdle;
        rebuildCache();
    }

    @Override
    public void close()  {
        itemCache.close();
    }

    @Override
    protected void fireItemSetChange(ItemSetChangeEvent event) {
        super.fireItemSetChange(event);

        updateCount(true);
        itemCache.clear();
    }

    public Bson getFilter() {
        return filter;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;
        fireItemSetChange();
    }

    private T getObject(Object itemId) {
        T ret = itemCache.get((Integer) itemId);
        if (ret != null) return ret;


        final int[] startingIndex = {(Integer) itemId};
        getIterable().skip(startingIndex[0]).limit(cachePageSizeLookAhead).forEach((Consumer<? super T>) t -> {
            itemCache.put(startingIndex[0]++, t);
        });

        return itemCache.get((Integer) itemId);
    }

    private FindIterable<T> getIterable(Object itemId) {
        return getIterable().skip((Integer)itemId).limit(1);
    }
    private FindIterable<T> getIterable() {
        return getIterable(null);
    }


    private FindIterable<T> getIterable(Bson addedFilter) {
        return getIterable(addedFilter, type);
    }

    private <A> FindIterable<A> getIterable(Bson addedFilter, Class<A> aClass) {
        FindIterable<A> iterable = mongoCollection.find(aClass);
        if (filter != null || addedFilter != null) {
            if (filter == null) {
                iterable = iterable.filter(addedFilter);
            }
            else {
                if (addedFilter != null) {
                    iterable = iterable.filter(Filters.and(filter, addedFilter));
                }
                else {
                    iterable = iterable.filter(filter);
                }
            }
        }
        iterable = iterable.sort(sort == null ? Sorts.ascending("_id") : sort);

        return iterable;
    }
    private Optional<String> getDatabaseNameFromProperty(Object property) {
        if (property instanceof String) {
            return Optional.ofNullable(pojoCodec.getTranslateBeanToDatabase().get(property));
        }

        return Optional.empty();
    }

    @Override
    public Item getItem(Object itemId) {
        updateCount(false);
        return new BeanItem<T>(getObject(itemId));
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        updateCount(false);
        return names;
    }

    @Override
    public Collection<?> getItemIds() {
        updateCount(false);
        return new IndexList(count);
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        updateCount(false);
        Item item = getItem(itemId);
        if (item == null) {
            return null;
        }
        return item.getItemProperty(propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        updateCount(false);
        return propertyDescriptors.get(names.indexOf((String)propertyId)).getReadMethod().getReturnType();
    }

    @Override
    public int size() {
        updateCount(false);
        return count;
    }

    @Override
    public boolean containsId(Object itemId) {
        updateCount(false);
        return (Integer)itemId < count;
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        updateCount(false);
        List<Bson> sorts = new ArrayList<>();
        for (int x = 0; x < propertyId.length; x++) {
            if (!getDatabaseNameFromProperty(propertyId[x]).isPresent()) continue;
            String dbField = getDatabaseNameFromProperty(propertyId[x]).get();

            if (ascending[x]) sorts.add(Sorts.ascending(dbField));
            else sorts.add(Sorts.descending(dbField));
        }

        sort = Sorts.orderBy(sorts);

        fireItemSetChange();
    }


    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        updateCount(false);
        return pojoCodec.getTranslateBeanToDatabase().keySet();
    }

    @Override
    public Object nextItemId(Object itemId) {
        updateCount(false);
        int id = (Integer)itemId;
        return id >= count ? null : id+1;
    }

    @Override
    public Object prevItemId(Object itemId) {
        updateCount(false);
        int id = (Integer)itemId;
        return id == 0 ? null : id-1;
    }

    @Override
    public Object firstItemId() {
        updateCount(false);
        return 0;
    }

    @Override
    public Object lastItemId() {
        updateCount(false);
        return count - 1;
    }

    @Override
    public boolean isFirstId(Object itemId) {
        updateCount(false);
        return firstItemId().equals(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        updateCount(false);
        return lastItemId().equals(itemId);
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOfId(Object itemId) {
        updateCount(false);
        return (Integer)itemId;
    }

    @Override
    public Object getIdByIndex(int index) {
        updateCount(false);
        return index;
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        updateCount(false);
        return new IndexList(numberOfItems, startIndex);
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


}

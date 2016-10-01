package com.awplab.core.mongodb.admin;

import com.awplab.core.mongodb.service.codec.BeanCodec;
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
import org.bson.BSON;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by andyphillips404 on 8/16/16.
 */
public class MongoCollectionContainer<DATABASE_BEAN_TYPE, CONTAINER_BEAN_TYPE> extends AbstractContainer implements Container.ItemSetChangeNotifier, Container.Sortable, Container.Indexed, AutoCloseable, Container.PropertySetChangeNotifier {


    public static abstract class ObjectTransferFunction<DATABASE_BEAN_TYPE, CONTAINER_BEAN_TYPE> {
        protected abstract CONTAINER_BEAN_TYPE transfer(DATABASE_BEAN_TYPE databaseObject);
    }


    @Override
    public void removeListener(ItemSetChangeListener listener) {
        super.removeListener(listener);
    }

    final private MongoCollection<DATABASE_BEAN_TYPE> mongoCollection;

    private Bson filter;

    private Bson sort = Sorts.ascending("_id");

    protected final Map<String, PropertyDescriptor> propertyDescriptors;

    //protected final List<String> names;

    private final BeanCodec<DATABASE_BEAN_TYPE> beanCodec;

    private final Class<DATABASE_BEAN_TYPE> databaseBeanType;

    private final Class<CONTAINER_BEAN_TYPE> gridBeanType;

    private UserManagedCache<Integer, CONTAINER_BEAN_TYPE> itemCache;

    private ObjectTransferFunction<DATABASE_BEAN_TYPE, CONTAINER_BEAN_TYPE> transferFunction;

    public static <A> MongoCollectionContainer<A, A> simpleContainer(MongoCollection<A> mongoCollection, Bson filter) {
        return new MongoCollectionContainer<A, A>(mongoCollection, mongoCollection.getDocumentClass(),null, filter);
    }

    public static <A> MongoCollectionContainer<A, A> simpleContainer(MongoCollection<A> mongoCollection) {
        return new MongoCollectionContainer<A, A>(mongoCollection, mongoCollection.getDocumentClass(), null, null);
    }

    public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class gridBeanType) throws IntrospectionException {
        return BeanUtil.getBeanPropertyDescriptor(gridBeanType).stream().filter(propertyDescriptor -> (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().getDeclaringClass() != Object.class)).collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
    }

    private Map<String, PropertyDescriptor> getPropertyDescriptors() throws IntrospectionException {
        return getPropertyDescriptors(this.gridBeanType);
    }

    public MongoCollectionContainer(MongoCollection<DATABASE_BEAN_TYPE> mongoCollection, Class<CONTAINER_BEAN_TYPE> gridBeanType, ObjectTransferFunction<DATABASE_BEAN_TYPE, CONTAINER_BEAN_TYPE> transferFunction, Bson filter)  {
        this.mongoCollection = mongoCollection;
        this.databaseBeanType = mongoCollection.getDocumentClass();
        this.filter = filter;
        this.gridBeanType = gridBeanType;

        try {
            this.propertyDescriptors = getPropertyDescriptors();
        }
        catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }

        //this.propertyDescriptors.addAll(BeanUtil.getBeanPropertyDescriptor(databaseBeanType));

        this.transferFunction = transferFunction;
        if (transferFunction == null) {
            this.transferFunction = new ObjectTransferFunction<DATABASE_BEAN_TYPE, CONTAINER_BEAN_TYPE>() {
                @Override
                protected CONTAINER_BEAN_TYPE transfer(DATABASE_BEAN_TYPE databaseObject) {
                    //noinspection unchecked
                    return (CONTAINER_BEAN_TYPE)databaseObject;
                }
            };
        }

        //this.names = this.propertyDescriptors.stream().map(FeatureDescriptor::getName).collect(Collectors.toList());

        this.beanCodec = new BeanCodec<>(databaseBeanType);

        rebuildCache();

        updateCount(true);

    }

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        super.addItemSetChangeListener(listener);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        super.addListener(listener);

    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        super.removeItemSetChangeListener(listener);

    }

    @Override
    public void addPropertySetChangeListener(PropertySetChangeListener listener) {
        super.addPropertySetChangeListener(listener);
    }

    @Override
    public void addListener(PropertySetChangeListener listener) {
        super.addListener(listener);
    }

    @Override
    public void removePropertySetChangeListener(PropertySetChangeListener listener) {
        super.removePropertySetChangeListener(listener);
    }

    @Override
    public void removeListener(PropertySetChangeListener listener) {
        super.removeListener(listener);
    }

    private void rebuildCache() {
        if (itemCache != null) {
            itemCache.clear();
            itemCache.close();
        }

        itemCache = UserManagedCacheBuilder.newUserManagedCacheBuilder(Integer.class, gridBeanType)
                .withExpiry(Expirations.<Integer, CONTAINER_BEAN_TYPE>timeToIdleExpiration(new org.ehcache.expiry.Duration(cacheTimeToIdle.toMillis(), TimeUnit.MILLISECONDS)))
                .build(true);
    }

    private int cachePageSizeLookAhead = 20;

    private Duration cacheTimeToIdle = Duration.ofSeconds(30);

    private Duration timeBetweenSizeUpdateChecks = Duration.ofSeconds(1);

    private int count = -1;
    private Date countLastUpdated = new Date(0);

    private void updateCount(boolean force) {
        if (force || !Duration.between(countLastUpdated.toInstant(), new Date().toInstant()).minus(timeBetweenSizeUpdateChecks).isNegative()) {

            int newCount = getCount(null);

            if (newCount != count) {
                count = newCount;
                if (!force) fireItemSetChange();
            }

            countLastUpdated = new Date();
        }
    }


    public int getCount(Bson addedFilter) {
        if (filter == null && addedFilter == null) {
            return (int) mongoCollection.count();
        }

        if (filter != null && addedFilter != null) {
            return (int) mongoCollection.count(Filters.and(filter, addedFilter));
        }

        if (filter != null) {
            return (int) mongoCollection.count(filter);
        }

        return (int)mongoCollection.count(addedFilter);

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
        updateCount(true);
        itemCache.clear();

        super.fireItemSetChange(event);

    }

    public Bson getFilter() {
        return filter;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;

        //rebuildCache();
        //updateCount(true);

        refreshData();
    }

    public DATABASE_BEAN_TYPE getDatabaseObject(Object itemId) {
        if (itemId instanceof Integer) {
            return getIterable().skip((Integer) itemId).limit(1).first();
        }
        throw new NumberFormatException("itemId must be an Integer!");
    }

    public CONTAINER_BEAN_TYPE getContainerObject(Object itemId) {

        CONTAINER_BEAN_TYPE ret = itemCache.get((Integer) itemId);
        if (ret != null) return ret;

        final int[] startingIndex = {(Integer) itemId};
        getIterable().skip(startingIndex[0]).limit(cachePageSizeLookAhead).forEach((Consumer<? super DATABASE_BEAN_TYPE>) d -> {
            CONTAINER_BEAN_TYPE wrapper = transferFunction.transfer(d);
            itemCache.put(startingIndex[0]++, wrapper);

        });

        return itemCache.get((Integer) itemId);
    }

    public FindIterable<DATABASE_BEAN_TYPE> getIterable(Object itemId) {
        return getIterable().skip((Integer)itemId).limit(1);
    }
    public FindIterable<DATABASE_BEAN_TYPE> getIterable() {
        return getIterable(null);
    }


    public FindIterable<DATABASE_BEAN_TYPE> getIterable(Bson addedFilter) {
        return getIterable(addedFilter, databaseBeanType);
    }

    public   <A> FindIterable<A> getIterable(Bson addedFilter, Class<A> aClass) {
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

    public Optional<String> getDatabaseNameFromProperty(Object property) {
        if (property instanceof String) {
            return Optional.ofNullable(beanCodec.getTranslateBeanToDatabase().get(property));
        }

        return Optional.empty();
    }

    @Override
    public Item getItem(Object itemId) {
        updateCount(false);
        return new BeanItem<CONTAINER_BEAN_TYPE>(getContainerObject(itemId));
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        updateCount(false);
        return propertyDescriptors.keySet();
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
        return propertyDescriptors.get(propertyId).getReadMethod().getReturnType();
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
        try {
            Map<String, PropertyDescriptor> baseDescriptors = getPropertyDescriptors();
            if (baseDescriptors.containsKey((String)propertyId)) {
                propertyDescriptors.put((String)propertyId, baseDescriptors.get((String)propertyId));

                refreshData();

                fireContainerPropertySetChange();

                return true;
            }

            return false;
        }
        catch (IntrospectionException ignored) {
            return false;
        }
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {

        if (!propertyDescriptors.containsKey((String)propertyId)) {
            return false;
        }

        propertyDescriptors.remove((String)propertyId);

        refreshData();

        fireContainerPropertySetChange();

        return true;
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

        Set<String> sortableNames = new HashSet<>(propertyDescriptors.keySet());
        return sortableNames.stream().filter(s -> beanCodec.getTranslateBeanToDatabase().containsKey(s)).collect(Collectors.toList());
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


    public void refreshData() {
        fireItemSetChange();
    }
}

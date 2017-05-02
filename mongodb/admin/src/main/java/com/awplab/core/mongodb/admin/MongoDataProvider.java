package com.awplab.core.mongodb.admin;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by andyphillips404 on 4/26/17.
 */
public class MongoDataProvider<T> extends AbstractBackEndDataProvider<T, Bson> {

    final private MongoCollection<T> mongoCollection;

    private Bson baseFilter;

    public Bson getBaseFilter() {
        return baseFilter;
    }

    public void setBaseFilter(Bson baseFilter) {
        this.baseFilter = baseFilter;
        this.refreshAll();
    }

    public MongoDataProvider(MongoCollection<T> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    private Bson defaultSort = Sorts.ascending("_id");

    public Bson getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(Bson defaultSort) {
        if (defaultSort == null) this.defaultSort = Sorts.ascending("_id");
        this.defaultSort = defaultSort;
        this.refreshAll();
    }

    public MongoDataProvider(MongoCollection<T> mongoCollection, Bson baseFilter) {
        this.mongoCollection = mongoCollection;
        this.baseFilter = baseFilter;
    }

    private Bson getSorts(List<QuerySortOrder> list) {
        if (list == null || list.size() == 0) return defaultSort;

        List<Bson> newSorts = new ArrayList<>();
        for (QuerySortOrder querySortOrder : list) {
            if (querySortOrder.getDirection() == null || querySortOrder.getDirection().equals(SortDirection.ASCENDING)) newSorts.add(Sorts.ascending(querySortOrder.getSorted()));
            else newSorts.add(Sorts.descending(querySortOrder.getSorted()));
        }
        return Sorts.orderBy(newSorts);

    }


    private Bson getFilter(Query<T, Bson> query) {
        if (query.getFilter().isPresent()) {
            if (baseFilter == null) return query.getFilter().get();
            return Filters.and(baseFilter, query.getFilter().get());
        }
        return null;
    }

    protected List<T> fetchDataFromBackEnd(Query<T, Bson> query) {
        Bson filter = getFilter(query);
        FindIterable<T> findIterable = mongoCollection.find();
        if (filter != null) findIterable = findIterable.filter(filter);
        findIterable = findIterable.skip(query.getOffset());
        findIterable = findIterable.limit(query.getLimit());
        findIterable = findIterable.sort(getSorts(query.getSortOrders()));

        final List<T> data = new ArrayList<T>();
        findIterable.iterator().forEachRemaining(data::add);
        return data;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Bson> query) {
        /* do not change below, only update fetchDataFromBackEnd */
        return fetchDataFromBackEnd(query).stream();
    }

    @Override
    protected int sizeInBackEnd(Query<T, Bson> query) {
        Bson filter = getFilter(query);
        if (filter != null) return (int)mongoCollection.count(filter);
        else return (int)mongoCollection.count();

    }

    public MongoCollection<T> getMongoCollection() {
        return mongoCollection;
    }
}

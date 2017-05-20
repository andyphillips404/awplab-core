package com.awplab.core.mongodb.service.codec;

import com.awplab.core.mongodb.service.BeanCodecInclude;
import com.awplab.core.mongodb.service.BeanCodecKey;
import com.awplab.core.mongodb.service.BeanCodecProperties;
import com.mongodb.MongoClient;
import net.jodah.typetools.TypeResolver;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by andyphillips404 on 5/29/16.
 */
public class BeanCodec<T> implements CollectibleCodec<T> {

    private static final String ID_FIELD_NAME = "_id";

    private final CodecRegistry codecRegistry;
    private final BsonTypeCodecMap bsonTypeCodecMap;

    private final Class<T> type;

    private final Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();


    private final Map<String, BeanCodecInclude> keyIncludeLookup = new HashMap<>();

    private final Map<String, String> translateBeanToDatabase = new HashMap<>();

    private boolean ignoreUnknown = true;

    public Map<String, BeanCodecInclude> getKeyIncludeLookup() {
        return keyIncludeLookup;
    }

    public boolean isIgnoreUnknown() {
        return ignoreUnknown;
    }

    public Map<String, String> getTranslateBeanToDatabase() {
        return translateBeanToDatabase;
    }

    public BeanCodec(Class<T> type) {
        this(type, MongoClient.getDefaultCodecRegistry());
    }

    public BeanCodec(Class<T> type, CodecRegistry registry) {
        this.type = type;
        this.codecRegistry = registry;
        this.bsonTypeCodecMap = new BsonTypeCodecMap(new BsonTypeClassMap(), codecRegistry);

        BeanCodecProperties beanCodecProperties = type.getAnnotation(BeanCodecProperties.class);


        boolean autoDetect = true;
        BeanCodecInclude defaultInclude = BeanCodecInclude.NOT_EMPTY;
        boolean ignoreInherited = false;
        boolean ignoreReadOnly = true;

        if (beanCodecProperties != null) {
            ignoreUnknown = beanCodecProperties.ignoreUnknown();
            autoDetect = beanCodecProperties.autoDetect();
            ignoreReadOnly = beanCodecProperties.ignoreReadOnly();
            defaultInclude = beanCodecProperties.defaultInclude();
            ignoreInherited = beanCodecProperties.ignoreInherited();
        }

        if (defaultInclude == BeanCodecInclude.DEFAULT) {
            throw new RuntimeException("Class level BeanCodecInclude may not be DEFAULT");
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            List<PropertyDescriptor> allPropertyDescriptors = Arrays.asList(beanInfo.getPropertyDescriptors());
            for (PropertyDescriptor propertyDescriptor : allPropertyDescriptors) {
                if (propertyDescriptor.getReadMethod() == null) continue;
                if (propertyDescriptor.getWriteMethod() == null && ignoreReadOnly) continue;
                if (propertyDescriptor.getReadMethod().getDeclaringClass() == Object.class) continue;
                if (ignoreInherited && propertyDescriptor.getReadMethod().getDeclaringClass() != type) continue;

                BeanCodecKey beanCodecKey = propertyDescriptor.getReadMethod().getAnnotation(BeanCodecKey.class);
                if (beanCodecKey == null && propertyDescriptor.getWriteMethod() != null) beanCodecKey = propertyDescriptor.getWriteMethod().getAnnotation(BeanCodecKey.class);
                if (beanCodecKey == null && Arrays.stream(type.getDeclaredFields()).anyMatch(field -> field.getName().equals(propertyDescriptor.getName()))) {
                    beanCodecKey = type.getDeclaredField(propertyDescriptor.getName()).getAnnotation(BeanCodecKey.class);
                }

                if (beanCodecKey == null && !autoDetect) continue;
                if (beanCodecKey != null && beanCodecKey.ignore()) continue;

                String databaseKey = (beanCodecKey == null || beanCodecKey.value().equals(BeanCodecKey.DEFAULT_VALUE)) ? propertyDescriptor.getName() : beanCodecKey.value();
                translateBeanToDatabase.put(propertyDescriptor.getName(), databaseKey);
                propertyDescriptors.put(databaseKey, propertyDescriptor);
                keyIncludeLookup.put(databaseKey, (beanCodecKey == null ? defaultInclude : beanCodecKey.include()));
            }

        } catch (Exception e) {
            throw new RuntimeException("Exceptions attempting to dissemble pojo information", e);
        }


    }




    private Object readValue(final BsonReader reader, final DecoderContext decoderContext, Class setType, Type genericTypeIfKnown) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {


            if (setType.isArray()) {
                List list = readList(reader, decoderContext, setType.getComponentType());
                Object primArray = Array.newInstance(setType.getComponentType(), list.size());
                for (int x = 0; x < list.size(); x++) {
                    Array.set(primArray, x, list.get(x));
                }
                return primArray;
            }
            else {
                // assumed to be a collection of some type, find the parent type if able to...
                if (setType.equals(Object.class) || genericTypeIfKnown instanceof ParameterizedType) {
                    return readList(reader, decoderContext, setType.equals(Object.class) ? Object.class : (Class)((ParameterizedType) genericTypeIfKnown).getActualTypeArguments()[0]);
                }

                Class<?>[] args = TypeResolver.resolveRawArguments(Collection.class, setType);
                if (args == null || args.length == 0) {
                    throw new BeanCodecDecodeException("Unable to decode array into collection type, unable to determine generate type");
                }
                return readList(reader, decoderContext, args[0]);


            }

        } else if (bsonType == BsonType.BINARY) {
            byte bsonSubType = reader.peekBinarySubType();
            if (bsonSubType == BsonBinarySubType.UUID_STANDARD.getValue() || bsonSubType == BsonBinarySubType.UUID_LEGACY.getValue()) {
                return codecRegistry.get(UUID.class).decode(reader, decoderContext);
            }
        }

        if (bsonType == BsonType.DOCUMENT && (Map.class.isAssignableFrom(setType) || setType.equals(Object.class))) {

            if (setType.equals(Object.class) || genericTypeIfKnown instanceof ParameterizedType) {
                return readMap(reader, decoderContext, setType.equals(Object.class) ? Object.class : (Class) ((ParameterizedType) genericTypeIfKnown).getActualTypeArguments()[1]);
            }

            // asummed to be a map, get the value parameter type.
            Class<?>[] args = TypeResolver.resolveRawArguments(Map.class, setType);
            if (args == null || args.length != 2) {
                throw new BeanCodecDecodeException("Unable to resolve parameter types of map");
            }
            //if (!String.class.isAssignableFrom(args[0])) {
                //throw new BeanCodecDecodeException("First parameter type of map must be assignable to string");
            //}

            return readMap(reader, decoderContext, args[1].equals(TypeResolver.Unknown.class) ? Object.class : args[1]);
            //return readMap(reader, decoderContext, args[1]);

        }

            // this is a document type, lets see if it is a sub document....
            Decoder decoder = codecRegistry.get(setType);
            if (decoder == null) {
                throw new BeanCodecDecodeException("No decoder found to handle document object for class type: " + setType);
            }
            if (bsonType != BsonType.DOCUMENT && decoder instanceof BeanCodec) {
                // we are not a document and trying to use the bean codec.
                // don't allow this.  try the standard codec by bson type then
                decoder = bsonTypeCodecMap.get(bsonType);
            }
            return decoder.decode(reader, decoderContext);
        //}


        //return bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext);
    }

    private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext, Class setType) {
        reader.readStartArray();
        List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext, setType, null));
        }
        reader.readEndArray();
        return list;
    }

    private Map readMap(final BsonReader reader, final DecoderContext decoderContext, Class setType) {
        reader.readStartDocument();
        Map<String, Object> map = new HashMap<String, Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            map.put(fieldName, readValue(reader, decoderContext, setType, null));
        }
        reader.readEndDocument();
        return map;
    }


    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {

        try {
            T instance = (T) type.newInstance();

            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String key = reader.readName();

                PropertyDescriptor propertyDescriptor = propertyDescriptors.get(key);
                if (propertyDescriptor == null || propertyDescriptor.getWriteMethod() == null) {
                    if (ignoreUnknown) continue;
                    else {
                        throw new BeanCodecDecodeException("Property " + key + " is unknown or writable and ingoreUnknown is false.");
                    }
                }

                Object value = readValue(reader, decoderContext, propertyDescriptor.getPropertyType(), propertyDescriptor.getReadMethod().getGenericReturnType());
                if (value == null) continue;

                if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType()) && Collection.class.isAssignableFrom(value.getClass())) {

                    Collection<?> collection = null;
                    if (!propertyDescriptor.getPropertyType().isInterface()) {
                        collection = (Collection) propertyDescriptor.getPropertyType().newInstance();
                    }
                    else {
                        for (Class<?> collectionClass : Arrays.asList(HashSet.class, ArrayList.class, LinkedList.class)) {
                            if (propertyDescriptor.getPropertyType().isAssignableFrom(collectionClass)) {
                                collection = (Collection)collectionClass.newInstance();
                                break;
                            }
                        }

                    }
                    collection.addAll((Collection) value);
                    propertyDescriptor.getWriteMethod().invoke(instance, collection);
                } else {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(instance, value);
                    } catch (Exception ex) {
                        throw new BeanCodecDecodeException("Exception decoding Property " + key + " type " + propertyDescriptor.getPropertyType().toString() + " for return type " + value.getClass().toString(), ex);
                    }
                }
            }

            reader.readEndDocument();

            return instance;

        } catch (Exception ex) {
            throw new BeanCodecDecodeException("Exception decoding pojo.", ex);
        }



    }

    public Object getValue(T document, String key) {

        if (!propertyDescriptors.containsKey(key)) return null;

        try {
            return propertyDescriptors.get(key).getReadMethod().invoke(document);
        }
        catch (Exception ignored) {
            return null;
        }

    }


    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        Document document = new Document();

        for (String key : propertyDescriptors.keySet()) {
            BeanCodecInclude include = keyIncludeLookup.get(key);

            try {
                Object docValue = getValue(value, key);

                if (include != BeanCodecInclude.NEVER &&
                        ((docValue == null && include == BeanCodecInclude.ALWAYS) ||
                                (docValue != null &&
                                        (include == BeanCodecInclude.NOT_NULL ||
                                                ((docValue instanceof Collection &&
                                                        ((Collection) docValue).size() > 0) ||
                                                        (docValue.getClass().isArray() && Array.getLength(docValue) > 0) ||
                                                        ((!(docValue instanceof Collection) && !docValue.getClass().isArray()))))))) {

                    if (docValue != null && docValue.getClass().isArray()) {
                        ArrayList arrayList = new ArrayList();
                        for (int x = 0; x < Array.getLength(docValue); x++) {
                            arrayList.add(Array.get(docValue, x));
                        }
                        document.put(key, arrayList);
                    } else {
                        document.put(key, docValue);
                    }

                }
            }
            catch (Exception ex) {
                throw new BeanCodecEncodeException("Unable to encode key " + key, ex);

            }


        }

        codecRegistry.get(Document.class).encode(writer, document, encoderContext);
        //documentCodec.encode(writer, document, encoderContext);


    }


    @Override
    public Class<T> getEncoderClass() {
        return type;
    }




    @Override
    public T generateIdIfAbsentFromDocument(T document) {
        if (!documentHasId(document)) {

            try {
                if (propertyDescriptors.containsKey(ID_FIELD_NAME) && propertyDescriptors.get(ID_FIELD_NAME).getWriteMethod() != null) {
                    if (propertyDescriptors.get(ID_FIELD_NAME).getPropertyType() != ObjectId.class) {
                        throw new RuntimeException("_id field must be of type ObjectId!");
                    }
                    propertyDescriptors.get(ID_FIELD_NAME).getWriteMethod().invoke(document, ObjectId.get());
                }
                else {
                    throw new RuntimeException("Unable to generate new ObjectId, field doesn't exist or is not writable");
                }
            }
            catch (Exception ex) {
                throw new RuntimeException("Unable to generate new ObjectId", ex);
            }
        }

        return document;
    }

    @Override
    public boolean documentHasId(T document) {
        return (propertyDescriptors.containsKey(ID_FIELD_NAME) && propertyDescriptors.get(ID_FIELD_NAME).getWriteMethod() != null && getValue(document, ID_FIELD_NAME) != null);
    }

    @Override
    public BsonValue getDocumentId(T document) {

        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain the _id field!");
        }

        Object id = getValue(document, ID_FIELD_NAME);

        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }

        return new BsonObjectId((ObjectId)id);
    }
}

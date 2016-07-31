package com.awplab.core.mongodb.codec;

import com.awplab.core.mongodb.PojoCodecInclude;
import com.awplab.core.mongodb.PojoCodecKey;
import com.awplab.core.mongodb.PojoCodecProperties;
import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by andyphillips404 on 5/29/16.
 */
public class PojoCodec<T> implements CollectibleCodec<T> {

    private final Codec<Document> documentCodec;
    private final Class<T> type;

    private final Map<String, Object> setKeyLookup = new HashMap<>();
    private final Map<String, Object> getKeyLookup = new HashMap<>();
    private final Map<String, PojoCodecInclude> keyIncludeLookup = new HashMap<>();

    private boolean ignoreUnknown = true;


    public PojoCodec(Class<T> type, Codec<Document> documentCodec) {
        this.type = type;
        this.documentCodec = documentCodec;

        PojoCodecProperties pojoCodecProperties = type.getAnnotation(PojoCodecProperties.class);

        boolean autoDetectFields = true;
        boolean autoDetectMethods = true;
        PojoCodecInclude defaultInclude = PojoCodecInclude.NOT_EMPTY;
        boolean ignoreInherited = false;
        boolean ignoreStaticFields = true;
        boolean ignoreFinalFields = false;

        if (pojoCodecProperties != null) {
            ignoreUnknown = pojoCodecProperties.ignoreUnknown();
            autoDetectFields = pojoCodecProperties.autoDetectFields();
            autoDetectMethods = pojoCodecProperties.autoDetectMethods();
            defaultInclude = pojoCodecProperties.defaultInclude();
            ignoreInherited = pojoCodecProperties.ignoreInherited();
            ignoreStaticFields = pojoCodecProperties.ignoreStaticFields();
            ignoreFinalFields = pojoCodecProperties.ignoreFinalFields();
        }

        if (defaultInclude == PojoCodecInclude.DEFAULT) {
            throw new RuntimeException("Class level PojoCodecInclude may not be DEFAULT");
        }

        for (Field field : type.getFields()) {
            if (ignoreInherited && !field.getDeclaringClass().equals(type)) continue;
            if (ignoreStaticFields && Modifier.isStatic(field.getModifiers())) continue;
            if (ignoreFinalFields && Modifier.isFinal(field.getModifiers())) continue;

            PojoCodecKey pojoCodecKey = field.getAnnotation(PojoCodecKey.class);
            if (pojoCodecKey != null) {
                if (pojoCodecKey.ignore()) continue;

                String key = (pojoCodecKey.value().equals(PojoCodecKey.DEFAULT_VALUE) ? field.getName() : pojoCodecKey.value());
                if (!Modifier.isFinal(field.getModifiers())) setKeyLookup.put(key, field);
                getKeyLookup.put(key, field);
                keyIncludeLookup.put(key, (pojoCodecKey.include() == PojoCodecInclude.DEFAULT ? defaultInclude : pojoCodecKey.include()));
            }
            else {
                if (autoDetectFields) {
                    String key = field.getName();
                    if (!Modifier.isFinal(field.getModifiers())) setKeyLookup.put(key, field);
                    getKeyLookup.put(key, field);
                    keyIncludeLookup.put(key, defaultInclude);
                }
            }
        }

        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class)) continue;
            if (ignoreInherited && !method.getDeclaringClass().equals(type)) continue;

            PojoCodecKey pojoCodecKey = method.getAnnotation(PojoCodecKey.class);
            if (!method.getReturnType().equals(Void.TYPE) && method.getParameterCount() == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
                String methodKey = (method.getName().startsWith("get") ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3));
                if (pojoCodecKey == null) {
                    try {
                        Field privateField = type.getDeclaredField(methodKey);
                        if (!privateField.isAccessible()) {
                            pojoCodecKey = privateField.getAnnotation(PojoCodecKey.class);
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                if (pojoCodecKey != null) {
                    if (pojoCodecKey.ignore()) continue;

                    String key = (pojoCodecKey.value().equals(PojoCodecKey.DEFAULT_VALUE) ? methodKey : pojoCodecKey.value());
                    getKeyLookup.put(key, method);
                    keyIncludeLookup.put(key, (pojoCodecKey.include() == PojoCodecInclude.DEFAULT ? defaultInclude : pojoCodecKey.include()));
                }
                else {
                    if (autoDetectMethods) {
                        getKeyLookup.put(methodKey, method);
                        keyIncludeLookup.put(methodKey, defaultInclude);
                    }
                }
            }
            else {
                if (method.getReturnType().equals(Void.TYPE) && method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                    String methodKey = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                    if (pojoCodecKey == null) {
                        try {
                            Field privateField = type.getDeclaredField(methodKey);
                            if (!privateField.isAccessible()) {
                                pojoCodecKey = privateField.getAnnotation(PojoCodecKey.class);
                            }
                        } catch (NoSuchFieldException ignored) {
                        }
                    }
                    if (pojoCodecKey != null) {
                        if (pojoCodecKey.ignore()) continue;

                        String key = (pojoCodecKey.value().equals(PojoCodecKey.DEFAULT_VALUE) ? methodKey : pojoCodecKey.value());
                        setKeyLookup.put(key, method);
                    }
                    else {
                        if (autoDetectMethods) {
                            setKeyLookup.put(methodKey, method);
                        }
                    }

                }
                else {
                    if (pojoCodecKey != null && !pojoCodecKey.ignore()) {
                        throw new RuntimeException("Anotated method does not match standard POJO method naming convention or structure, method: " + method.getName());
                    }

                }

            }


        }
    }


    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);

        try {
            T instance = (T)type.newInstance();

            for (String key : document.keySet()) {
                Object value = document.get(key);

                Object fieldOrMethod = setKeyLookup.get(key);
                if (fieldOrMethod == null) {
                    if (ignoreUnknown) continue;
                    else {
                        throw new PojoCodecDecodeException("Field " + key + " is unknown and ingoreUnknown is false.");
                    }
                }

                if (fieldOrMethod instanceof Field) {
                    try {
                        Field field = (Field)fieldOrMethod;

                        if (value.getClass().isAssignableFrom(field.getType())) {
                            field.set(instance, value); //document.get(key, field.getType()));
                        }
                        else {
                            if (Collection.class.isAssignableFrom(field.getType()) && Collection.class.isAssignableFrom(value.getClass())) {
                                Collection<?> collection = (Collection)field.getType().newInstance();
                                collection.addAll((Collection)value);
                                field.set(instance, collection);
                            }
                            else {
                                throw new PojoCodecDecodeException("Field " + key + " type " + field.getType().toString() + " is not assignable for return type " + value.getClass().toString());
                            }
                        }

                    }
                    catch (Exception ex) {
                        throw new PojoCodecDecodeException("Exception attempting to set field " + key + ".", ex);
                    }
                }
                else {
                    if (fieldOrMethod instanceof Method) {
                        Method method = (Method) fieldOrMethod;
                        try {
                            method.invoke(instance, value);
                        } catch (Exception e) {
                            Class<?> setClass = method.getParameters()[0].getType();
                            if (Collection.class.isAssignableFrom(setClass) && Collection.class.isAssignableFrom(value.getClass())) {
                                Collection<?> collection = (Collection)setClass.newInstance();
                                collection.addAll((Collection)value);
                                method.invoke(instance, collection);
                            }
                            else {
                                throw new PojoCodecDecodeException("Exception attempting to execute method " + ((Method) fieldOrMethod).getName() + " for field " + key, e);
                            }
                        }
                    }
                }
            }

            return instance;

        } catch (Exception ex) {
            throw new PojoCodecDecodeException("Exception decoding pojo.", ex);
        }

    }

    private Object getValue(String key, T value) {
        Object fieldOrMethod = getKeyLookup.get(key);

        Object docValue = null;

        if (fieldOrMethod instanceof Field) {
            try {
                docValue = ((Field) fieldOrMethod).get(value);
            } catch (IllegalAccessException e) {
                throw new PojoCodecEncodeException(e);
            }

        }

        if (fieldOrMethod instanceof Method) {
            try {
                docValue = ((Method) fieldOrMethod).invoke(value);
            } catch (Exception e) {
                throw new PojoCodecEncodeException(e);
            }
        }

        return docValue;
    }
    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        Document document = new Document();

        for (String key : getKeyLookup.keySet()) {
            PojoCodecInclude include = keyIncludeLookup.get(key);

            Object docValue = getValue(key, value);


            if (include != PojoCodecInclude.NEVER &&
                    ((docValue == null && include == PojoCodecInclude.ALWAYS) ||
                            (docValue != null &&
                            (include == PojoCodecInclude.NOT_NULL ||
                                    ((docValue instanceof Collection  &&
                                            ((Collection) docValue).size() > 0) ||
                                            (docValue.getClass().isArray() && Arrays.asList(docValue).size() > 0) ||
                                            ((!(docValue instanceof Collection) && !docValue.getClass().isArray()))))))) {

                document.put(key, docValue);

            }


        }

        documentCodec.encode(writer, document, encoderContext);


    }

    @Override
    public Class<T> getEncoderClass() {
        return type;
    }




    @Override
    public T generateIdIfAbsentFromDocument(T document) {
        if (!documentHasId(document)) {
            Object fieldOrMethod = setKeyLookup.get("_id");
            if (fieldOrMethod instanceof Field) {
                if (((Field) fieldOrMethod).getType().isAssignableFrom(ObjectId.class)) {
                    try {
                        ((Field) fieldOrMethod).set(document, ObjectId.get());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to generate new ", e);
                    }
                }
                else {
                    throw new RuntimeException("_id field must be of type ObjectId!");
                }
            }

            if (fieldOrMethod instanceof Method) {
                try {
                    ((Method) fieldOrMethod).invoke(document, ObjectId.get());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to generate new ObjectId");
                }
            }
        }

        return document;
    }

    @Override
    public boolean documentHasId(T document) {
        return (getKeyLookup.containsKey("_id") && setKeyLookup.containsKey("_id") && getValue("_id", document) != null);
    }

    @Override
    public BsonValue getDocumentId(T document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain the _id field!");
        }
        return new BsonObjectId((ObjectId)getValue("_id", document));
    }
}

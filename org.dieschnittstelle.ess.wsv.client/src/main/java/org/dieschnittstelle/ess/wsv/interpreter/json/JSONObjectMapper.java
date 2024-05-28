package org.dieschnittstelle.ess.wsv.interpreter.json;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.logging.log4j.Logger;

/**
 * 
 * CAUTION: THIS IS A RATHER QUICK&DIRTY SOLUTION... !!!
 * 
 */
public class JSONObjectMapper {

	protected static final Logger logger = org.apache.logging.log4j.LogManager
			.getLogger(JSONObjectMapper.class);

	private static JSONObjectMapper instance;

	/**
	 * private constructor. I.e. this class can only be accessed via
	 * getInstance() to ensure its singleton status
	 */
	private JSONObjectMapper() {

	}

	/**
	 * obtain an instance of this class
	 * 
	 * @return
	 */
	public static synchronized JSONObjectMapper getInstance() {
		if (instance == null) {
			instance = new JSONObjectMapper();
		}

		return instance;
	}

	/**
	 * create a json node given some value (recursive)
	 * 
	 * @param value
	 * @return
	 */

	protected JsonNode tojson(Object value) throws ObjectMappingException {

		logger.debug("convert value " + value + " of class " + value.getClass()
				+ " to json: " + value);

		try {
			if (value == null) {
				logger.debug("ignore null value...");
				return null;
			} else if (value instanceof Collection) {
				logger.debug("convert List to ArrayNode: " + value);
				ArrayNode jsonlist = JsonNodeFactory.instance.arrayNode();
				for (Object item : ((Collection<?>) value)) {
					jsonlist.add(tojson(item));
				}
				return jsonlist;
			} else if (!(value instanceof Object) || (value instanceof String)
					|| (value instanceof Integer) || (value instanceof Long)
					|| (value instanceof Boolean)) {
				logger.debug("convert primitive datatype to TextNode: " + value);
				return JsonNodeFactory.instance.textNode(String.valueOf(value));
			}
			// enumerations need a special treatment...
			else if (value.getClass().isEnum()) {
				return JsonNodeFactory.instance.textNode(String.valueOf(value));
			} else if (value instanceof Object) {
				logger.debug("convert value " + value + " of class "
						+ value.getClass() + " to ObjectNode: " + value);
				// create an objectnode
				ObjectNode json = JsonNodeFactory.instance.objectNode();

				// check whether on the superclass we have the JsonTypeInfo
				// present, i.e. we will need to add the class name as meta
				// information on the json obj
				// this should be done recursively upwards towards object!
				if (value.getClass().getSuperclass()
						.isAnnotationPresent(JsonTypeInfo.class)) {
					logger.info("we need to add the type info to the json object...");
					JsonTypeInfo typeInfo = value.getClass().getSuperclass()
							.getAnnotation(JsonTypeInfo.class);
					if (typeInfo.include() == JsonTypeInfo.As.PROPERTY
							&& typeInfo.use() == JsonTypeInfo.Id.CLASS) {
						json.put(typeInfo.property(), value.getClass()
								.getName());
					}
				}

				// we iterate over the methods and look for the getters
				for (Method meth : value.getClass().getMethods()) {
					if (meth.getName().startsWith("get")
							&& !Modifier.isStatic(meth.getModifiers())
							&& !"getClass".equals(meth.getName())) {
						logger.debug("found getter: " + meth.getName());

						// a getter is a method that starts with "get" and has
						// no
						// arguments...
						if (meth.getName().startsWith("get")
								&& meth.getParameterTypes().length == 0) {
							// then we determine the attribute name
							String fieldname = meth.getName().substring(
									"get".length());
							// apply getter naming conventions to determine the
							// name
							// of the field
							fieldname = fieldname.substring(0, 1).toLowerCase()
									+ fieldname.substring(1);
							logger.debug("invoke: " + meth);

							Object fieldvalue = meth.invoke(value,
									new Object[] {});
							if (fieldvalue != null) {
								// and we set the attribute value in the json
								// object
								// to
								// the return value of a recursive call to
								// ourselves
								JsonNode jsonFieldvalue = tojson(fieldvalue);
								if (jsonFieldvalue != null) {
									json.put(fieldname, jsonFieldvalue);
								}
							}
						}
					}
				}

				return json;
			} else {
				System.err
						.println("cannot process unknown implementation of IComplexTypeInstance: "
								+ value);
				return JsonNodeFactory.instance.nullNode();
			}
		} catch (ObjectMappingException e) {
			throw e;
		} catch (Exception e) {
			logger.error("got exception: " + e);
			throw new ObjectMappingException(e);
		}
	}

	/**
	 * create an object from a json node
	 * 
	 * @param json
	 * @param type of object to be created - we assume that this is either a
	 *        class or a parameterized type whose raw type is a class
	 * @return
	 * @throws ObjectMappingException
	 */
	protected Object fromjson(JsonNode json, Type type)
			throws ObjectMappingException {

		try {
			if (json == null) {
				return null;
			} else if (json instanceof NullNode) {
				return null;
			} else if (json instanceof BooleanNode) {
				return ((BooleanNode) json).booleanValue();
			} else if (json instanceof NumericNode) {
				if (type == Integer.TYPE) {
					return ((NumericNode) json).intValue();
				} else if (type == Long.TYPE) {
					return ((NumericNode) json).longValue();
				} else {
					return ((NumericNode) json).doubleValue();
				}
			} else if (json instanceof TextNode) {
				String text = ((TextNode) json).textValue();

				// so far we only support int, boolean and String fields
				if (type == Integer.TYPE) {
					return Integer.parseInt(text);
				} else if (type == Boolean.TYPE) {
					return Boolean.parseBoolean(text);

				} else if (type == Long.TYPE) {
					return Long.parseLong(text);
				}

				return text;
			} else if (json instanceof ObjectNode) {
				// in this case we do not foresee that we have generic types,
				// hence we cast the type argument to class

				// create a new instance of the class
				Object obj = null;

				// check whether we have an abstract class that has jsontype
				// info present
				if (Modifier.isAbstract(((Class) type).getModifiers())) {
					// TODO: include a handling for abstract classes considering
					// the JsonTypeInfo annotation that might be set on type
					JsonTypeInfo jsonTypeInfo = (JsonTypeInfo)((Class)type).getAnnotation(JsonTypeInfo.class);
					JsonNode classNode = json.get(jsonTypeInfo.property());

					if (classNode == null || !classNode.isTextual()) {
						throw new ObjectMappingException("Missing or invalid " + jsonTypeInfo.property() + " field for abstract class deserialization.");
					}

					Class clazz = Class.forName(classNode.asText());
					type = clazz;
					obj = clazz.newInstance();
				} else {
					obj = ((Class) type).newInstance();
				}

				// iterate over the fields in the json object and invoke the
				// corresponding setter on the instance
				for (Iterator<String> it = ((ObjectNode) json).fieldNames(); it
						.hasNext();) {
					String currentJsonField = it.next();

					// we exclude the meta field @class here
					if (!currentJsonField.startsWith("@")) {

						String capitalisedFieldname = currentJsonField
								.substring(0, 1).toUpperCase()
								+ currentJsonField.substring(1);
						// get the getter and determine its return type
						Method currentBeanFieldGetter = ((Class) type)
								.getMethod("get" + capitalisedFieldname,
										new Class[] {});
						Class currentBeanFieldClass = currentBeanFieldGetter
								.getReturnType();

						Object currentBeanFieldValue = fromjson(
								((ObjectNode) json).get(currentJsonField),
								currentBeanFieldClass);

						// we check whether we have an enumeration class!!!
						if (currentBeanFieldClass.isEnum()) {
							logger.debug("we have an enum value. need to convert it...");
							// lookup the valueof method
							Method valueOfMethod = currentBeanFieldClass
									.getDeclaredMethod("valueOf",
											new Class[] { String.class });
							currentBeanFieldValue = valueOfMethod.invoke(null,
									currentBeanFieldValue);
						}

						if (currentBeanFieldValue != null) {

							try {
								// invoke the setter method
								Method currentBeanFieldSetter = ((Class) type)
										.getMethod(
												"set" + capitalisedFieldname,
												new Class[] { currentBeanFieldClass });

								logger.debug("will invoke setter "
										+ currentBeanFieldSetter
										+ " for value " + currentBeanFieldValue);

								currentBeanFieldSetter.invoke(obj,
										currentBeanFieldValue);
							} catch (NoSuchMethodException e) {
								logger.debug("we got NoSuchMethodException: "
										+ e
										+ ". will check whether we have a collection typed attribute...");
								if (Collection.class
										.isAssignableFrom(currentBeanFieldClass)) {
									// check whether the value has more than one
									// element
									Iterator valueit = ((Collection) currentBeanFieldValue)
											.iterator();
									if (valueit.hasNext()) {
										// determine the class of the element
										Object firstElement = valueit.next();
										// we will add element-by-element
										Method currentBeanFieldAdder = ((Class) type)
												.getMethod(
														"add"
																+ capitalisedFieldname
																		.substring(
																				0,
																				capitalisedFieldname
																						.length() - 1),
														new Class[] { firstElement
																.getClass() });
										// invoke the adder
										currentBeanFieldAdder.invoke(obj,
												firstElement);
										while (valueit.hasNext()) {
											currentBeanFieldAdder.invoke(obj,
													valueit.next());
										}
									}
								}
							}

						}
					}
				}

				return obj;
			}
			// array handling is not really tested...
			else if (json instanceof ArrayNode) {
				Class concreteCollectionKlass;

				logger.debug("we found an array node: " + json
						+ "\n for class of type " + type);

				Class collectionClass = type instanceof Class ? (Class) type
						: (Class) ((ParameterizedType) type).getRawType();

				// if we have an abstract collection type, we will determine a
				// specfic one to instantiate here
				if (collectionClass == Collection.class
						|| collectionClass == Set.class) {
					concreteCollectionKlass = HashSet.class;
				} else if (collectionClass == List.class) {
					concreteCollectionKlass = ArrayList.class;
				} else {
					concreteCollectionKlass = collectionClass;
				}

				// if we have an array node, klass must be a Collection class,
				// i.e. instantiate it as such
				Collection col = (Collection) concreteCollectionKlass
						.newInstance();
				logger.debug("instantiated collection: " + col);

				// if we have a parameterized type, the first parameter will
				// tell us the type of the members of the collection
				if (type instanceof ParameterizedType) {
					Type memberType = ((ParameterizedType) type)
							.getActualTypeArguments()[0];

					logger.debug("member type of collection is: " + memberType);

					for (Object item : (ArrayNode) json) {
						col.add(fromjson((JsonNode) item, memberType));
					}

				} else {
					logger.warn("we have an array node, but do not know which type the members to convert to... use String as default...");

					for (Object item : (ArrayNode) json) {
						col.add(fromjson((JsonNode) item,
								String.class));
					}
				}

				return col;
			} else {
				throw new ObjectMappingException("Cannot handle json node: "
						+ json);
			}

		} catch (ObjectMappingException e) {
			throw e;
		} catch (Exception e) {
			logger.error("got exception: " + e);
			throw new ObjectMappingException(e);
		}

	}

}
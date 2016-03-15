package com.mdt.sso.view;

import com.sun.el.MethodExpressionImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.model.SelectItem;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.sql.DataSource;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import oracle.adf.controller.ControllerContext;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.model.binding.DCParameter;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.UIXTable;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;
import oracle.binding.ControlBinding;
import oracle.binding.OperationBinding;

import oracle.javatools.resourcebundle.BundleFactory;

import oracle.jbo.Key;
import oracle.jbo.NavigatableRowIterator;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.common.DefLocaleContext;
import oracle.jbo.domain.Timestamp;
import oracle.jbo.uicli.binding.JUCtrlValueBinding;

import org.apache.myfaces.trinidad.component.UIXEditableValue;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;


//import org.apache.log4j.Logger;


public class ADFUtils {
    public ADFUtils() {
        super();
    }

    // public static final Logger LOGGER = Logger.getLogger(ADFUtils.class);
    private static final String NO_RESOURCE_FOUND = "Missing resource: ";
    private static final String EXCEPTIONS_BUNDLE_NAME = "ExceptionMessages";
    private static ADFLogger logger = ADFLogger.createADFLogger(ADFUtils.class);

    public static final DefLocaleContext DEFAULT_LOCATE_CONTEXT =
        new DefLocaleContext(FacesContext.getCurrentInstance().getViewRoot().getLocale());

    /**
     * A convenience method for getting the value of a bound attribute in the
     * current page context programatically.
     * @param attributeName of the bound value in the pageDef
     * @return value of the attribute
     */
    public static Object getBoundAttributeValue(String attributeName) {
        return findControlBinding(attributeName).getInputValue();
    }

    /**
     * A convenience method for setting the value of a bound attribute in the
     * context of the current page.
     * @param attributeName of the bound value in the pageDef
     * @param value to set
     */
    public static void setBoundAttributeValue(String attributeName,
                                              Object value) {
        findControlBinding(attributeName).setInputValue(value);
    }

    /**
     * Returns the evaluated value of a pageDef parameter.
     * @param pageDefName reference to the page definition file of the page with the parameter
     * @param parameterName name of the pagedef parameter
     * @return evaluated value of the parameter as a String
     */
    public static Object getPageDefParameterValue(String pageDefName,
                                                  String parameterName) {
        BindingContainer bindings = findBindingContainer(pageDefName);
        DCParameter param =
            ((DCBindingContainer)bindings).findParameter(parameterName);
        return param.getValue();
    }

    /**
     * Convenience method to find a DCControlBinding as an AttributeBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param bindingContainer binding container
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(BindingContainer bindingContainer,
                                                      String attributeName) {
        if (attributeName != null) {
            if (bindingContainer != null) {
                ControlBinding ctrlBinding =
                    bindingContainer.getControlBinding(attributeName);
                if (ctrlBinding instanceof AttributeBinding) {
                    return (AttributeBinding)ctrlBinding;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to find a DCControlBinding as a JUCtrlValueBinding
     * to get able to then call getInputValue() or setInputValue() on it.
     * @param attributeName name of the attribute binding.
     * @return the control value binding with the name passed in.
     *
     */
    public static AttributeBinding findControlBinding(String attributeName) {
        return findControlBinding(getBindingContainer(), attributeName);
    }

    /**
     * Return the current page's binding container.
     * @return the current page's binding container
     */
    public static BindingContainer getBindingContainer() {
        BindingContext bctx = BindingContext.getCurrent();
        return bctx.getCurrentBindingsEntry();
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        return valueExp.getValue(elContext);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @author MK
     * @param facesContext FacesContext
     * @param expression EL expression
     * @return Managed object
     */
    public static Object resolveExpression(FacesContext facesContext,
                                           String expression) {
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        return valueExp.getValue(elContext);
    }

    public static String resolveRemoteUser() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext ectx = facesContext.getExternalContext();
        return ectx.getRemoteUser();
    }

    public static String getRequestURL() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request =
            (HttpServletRequest)externalContext.getRequest();
        return request.getRequestURL().toString();
    }

    public static String getContextRoot() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        return externalContext.getRequestContextPath();
    }

    public static String getRequestURLTillContextRoot() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request =
            (HttpServletRequest)externalContext.getRequest();

        return request.getRequestURL().toString().replace(request.getRequestURI().substring(0),
                                                          "") +
            request.getContextPath();
    }

    public static String resolveUserPrincipal() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext ectx = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        return request.getUserPrincipal().getName();
    }

    public static Object resloveMethodExpression(String expression,
                                                 Class returnType,
                                                 Class[] argTypes,
                                                 Object[] argValues) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        MethodExpression methodExpression =
            elFactory.createMethodExpression(elContext, expression, returnType,
                                             argTypes);
        return methodExpression.invoke(elContext, argValues);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching Boolean.
     * @param expression EL expression
     * @return Managed object
     */
    public static Boolean resolveExpressionAsBoolean(String expression) {
        return (Boolean)resolveExpression(expression);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching String (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static String resolveExpressionAsString(String expression) {
        return (String)resolveExpression(expression);
    }

    /**
     * Convenience method for resolving a reference to a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @return Managed object
     */
    public static Object getManagedBeanValue(String beanName) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        return resolveExpression(buff.toString());
    }

    /**
     * Method for setting a new object into a JSF managed bean
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValue(String expression, Object newValue) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);

        //Check that the input newValue can be cast to the property type
        //expected by the managed bean.
        //If the managed Bean expects a primitive we rely on Auto-Unboxing
        Class bindClass = valueExp.getType(elContext);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue) ||
            newValue == null) {
            valueExp.setValue(elContext, newValue);
        }
    }

    public static void setEL(String el, Object val) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory =
            facesContext.getApplication().getExpressionFactory();
        ValueExpression exp =
            expressionFactory.createValueExpression(elContext, el,
                                                    Object.class);

        exp.setValue(elContext, val);
    }

    /**
     * Convenience method for setting the value of a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @param newValue new value to set
     */
    public static void setManagedBeanValue(String beanName, String property,
                                           Object newValue) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append(".");
        buff.append(property);
        buff.append("}");
        setExpressionValue(buff.toString(), newValue);
    }


    /**
     * Convenience method for setting Session variables.
     * @param key object key
     * @param object value to store
     */

    public static void storeOnSession(String key, Object object) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        sessionState.put(key, object);
    }

    /**
     * Convenience method for getting Session variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromSession(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        return sessionState.get(key);
    }

    public static String getFromHeader(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        return ectx.getRequestHeaderMap().get(key);
    }

    /**
     * Convenience method for getting Request variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromRequest(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getRequestMap();
        return sessionState.get(key);
    }

    /**
     * Pulls a String resource from the property bundle that
     * is defined under the application &lt;message-bundle&gt; element in
     * the faces config. Respects Locale
     * @param key string message key
     * @return Resource value or placeholder error String
     */
    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return getStringSafely(bundle, key, null);
    }


    /**
     * Convenience method to construct a <code>FacesMesssage</code>
     * from a defined error key and severity
     * This assumes that the error keys follow the convention of
     * using <b>_detail</b> for the detailed part of the
     * message, otherwise the main message is returned for the
     * detail as well.
     * @param key for the error message in the resource bundle
     * @param severity severity of message
     * @return Faces Message object
     */
    public static FacesMessage getMessageFromBundle(String key,
                                                    FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key, null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message = new FacesMessage(summary, detail);
        message.setSeverity(severity);
        return message;
    }

    /**
     * Get view id of the view root.
     * @return view id of the view root
     */
    public static String getRootViewId() {
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    /**
     * Get component id of the view root.
     * @return component id of the view root
     */
    public static String getRootViewComponentId() {
        return FacesContext.getCurrentInstance().getViewRoot().getId();
    }


    /*
     * Internal method to pull out the correct local
     * message bundle
     */

    private static ResourceBundle getBundle() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIViewRoot uiRoot = ctx.getViewRoot();
        Locale locale = uiRoot.getLocale();
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(),
                                        locale, ldr);
    }

    /**
     * Get an HTTP Request attribute.
     * @param name attribute name
     * @return attribute value
     */
    public static Object getRequestAttribute(String name) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(name);
    }

    /**
     * Set an HTTP Request attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public static void setRequestAttribute(String name, Object value) {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(name,
                                                                                   value);
    }

    /*
     * Internal method to proxy for resource keys that don't exist
     */

    private static String getStringSafely(ResourceBundle bundle, String key,
                                          String defaultValue) {
        String resource = null;
        try {
            resource = bundle.getString(key);
        } catch (MissingResourceException mrex) {
            if (defaultValue != null) {
                resource = defaultValue;
            } else {
                resource = NO_RESOURCE_FOUND + key;
            }
        }
        return resource;
    }

    /**
     * Locate an UIComponent in view root with its component id. Use a recursive way to achieve this.
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    /**
     * Locate an UIComponent from its root component.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param base root Component (parent)
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId()))
            return base;

        UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() && (result == null)) {
            children = (UIComponent)childrens.next();
            if (id.equals(children.getId())) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Method to create a redirect URL. The assumption is that the JSF servlet mapping is
     * "faces", which is the default
     *
     * @param view the JSP or JSPX page to redirect to
     * @return a URL to redirect to
     */
    public static String getPageURL(String view) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        String url =
            ((HttpServletRequest)externalContext.getRequest()).getRequestURL().toString();
        StringBuffer newUrlBuffer = new StringBuffer();
        newUrlBuffer.append(url.substring(0, url.lastIndexOf("faces/")));
        newUrlBuffer.append("faces");
        String targetPageUrl = view.startsWith("/") ? view : "/" + view;
        newUrlBuffer.append(targetPageUrl);
        return newUrlBuffer.toString();
    }

    /**
     * Create value binding for EL exression
     * @param expression
     * @return Object
     */
    public static Object getExpressionObjectReference(String expression) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory =
            fc.getApplication().getExpressionFactory();
        return elFactory.createValueExpression(elctx, expression,
                                               Object.class).getValue(elctx);
    }


    /**
     * Invoke an expression
     * @param expr
     * @param returnType
     * @param argType
     * @param argument
     * @return
     */
    public static Object invokeMethodExpression(String expr, Class returnType,
                                                Class argType,
                                                Object argument) {
        return invokeMethodExpression(expr, returnType,
                                      new Class[] { argType },
                                      new Object[] { argument });

    }

    /**
     * Return the Binding Container as a DCBindingContainer.
     * @return current binding container as a DCBindingContainer
     */
    public static DCBindingContainer getDCBindingContainer() {
        return (DCBindingContainer)getBindingContainer();
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName name of the value attribute to use
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute to use for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(String iteratorName,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        return selectItemsForIterator(findIterator(iteratorName),
                                      valueAttrName, displayAttrName,
                                      descriptionAttrName);
    }

    /**
     * Get List of attribute values for an iterator.
     * @param iteratorName ADF iterator binding name
     * @param valueAttrName value attribute to use
     * @return List of attribute values for an iterator
     */
    public static List attributeListForIterator(String iteratorName,
                                                String valueAttrName) {
        return attributeListForIterator(findIterator(iteratorName),
                                        valueAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @param iteratorName iterabot binding name
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(String iteratorName) {
        return keyListForIterator(findIterator(iteratorName));
    }

    /**
     * Get List of Key objects for rows in an iterator.
     * @param iter iterator binding
     * @return List of Key objects for rows
     */
    public static List<Key> keyListForIterator(DCIteratorBinding iter) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getKey());
        }
        return attributeList;
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     * @param iteratorName iterator binding name
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(String iteratorName,
                                                   String keyAttrName) {
        return keyAttrListForIterator(findIterator(iteratorName), keyAttrName);
    }

    /**
     * Get List of Key objects for rows in an iterator using key attribute.
     *
     * @param iter iterator binding
     * @param keyAttrName name of key attribute to use
     * @return List of Key objects for rows
     */
    public static List<Key> keyAttrListForIterator(DCIteratorBinding iter,
                                                   String keyAttrName) {
        List<Key> attributeList = new ArrayList<Key>();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(new Key(new Object[] { r.getAttribute(keyAttrName) }));
        }
        return attributeList;
    }

    /**
     * Get a List of attribute values for an iterator.
     *
     * @param iter iterator binding
     * @param valueAttrName name of value attribute to use
     * @return List of attribute values
     */
    public static List attributeListForIterator(DCIteratorBinding iter,
                                                String valueAttrName) {
        List attributeList = new ArrayList();
        for (Row r : iter.getAllRowsInRange()) {
            attributeList.add(r.getAttribute(valueAttrName));
        }
        return attributeList;
    }

    /**
     * Find an iterator binding in the current binding container by name.
     *
     * @param name iterator binding name
     * @return iterator binding
     */
    public static DCIteratorBinding findIterator(String name) {
        DCIteratorBinding iter =
            getDCBindingContainer().findIteratorBinding(name);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + name + "' not found");
        }
        return iter;
    }

    /**
     * @param name
     * @return
     */
    public static JUCtrlValueBinding findCtrlBinding(String name) {
        JUCtrlValueBinding rowBinding =
            (JUCtrlValueBinding)getDCBindingContainer().findCtrlBinding(name);
        if (rowBinding == null) {
            throw new RuntimeException("CtrlBinding " + name + "' not found");
        }
        return rowBinding;
    }


    /**
     * Get List of ADF Faces SelectItem for an iterator binding with description.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with description
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName,
                                                          String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the value of the 'valueAttrName' attribute as the key for
     * the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param valueAttrName name of value attribute to use for key
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsForIterator(DCIteratorBinding iter,
                                                          String valueAttrName,
                                                          String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getAttribute(valueAttrName),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iteratorName ADF iterator binding name
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(String iteratorName,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        return selectItemsByKeyForIterator(findIterator(iteratorName),
                                           displayAttrName,
                                           descriptionAttrName);
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding with discription.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @param descriptionAttrName name of the attribute for description
     * @return ADF Faces SelectItem for an iterator binding with discription
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName,
                                                               String descriptionAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName),
                                           (String)r.getAttribute(descriptionAttrName)));
        }
        return selectItems;
    }

    /**
     * Get List of ADF Faces SelectItem for an iterator binding.
     *
     * Uses the rowKey of each row as the SelectItem key.
     *
     * @param iter ADF iterator binding
     * @param displayAttrName name of the attribute from iterator rows to display
     * @return List of ADF Faces SelectItem for an iterator binding
     */
    public static List<SelectItem> selectItemsByKeyForIterator(DCIteratorBinding iter,
                                                               String displayAttrName) {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (Row r : iter.getAllRowsInRange()) {
            selectItems.add(new SelectItem(r.getKey(),
                                           (String)r.getAttribute(displayAttrName)));
        }
        return selectItems;
    }

    /**
     * Find the BindingContainer for a page definition by name.
     *
     * Typically used to refer eagerly to page definition parameters. It is
     * not best practice to reference or set bindings in binding containers
     * that are not the one for the current page.
     *
     * @param pageDefName name of the page defintion XML file to use
     * @return BindingContainer ref for the named definition
     */
    private static BindingContainer findBindingContainer(String pageDefName) {
        BindingContext bctx = getDCBindingContainer().getBindingContext();
        BindingContainer foundContainer =
            bctx.findBindingContainer(pageDefName);
        return foundContainer;
    }

    //    /**
    //     * @param opList
    //     */
    //    public static void printOperationBindingExceptions(List opList) {
    //        if (opList != null && !opList.isEmpty()) {
    //            for (Object error : opList) {
    //                if (LOGGER.isInfoEnabled()) {
    //                    LOGGER.info(error.toString());
    //                }
    //            }
    //        }
    //    }

    /**
     * Generate a Snapshot Name for Application Module , to be used to save state
     * The Generated Snapshot Name is also pushed into the pageFlowScope in AppModuleStateMap
     *
     * @return String Generated Universal Unique Identifier
     */
    public static String generateUUID() {

        String uuid = UUID.randomUUID().toString();
        return uuid;

    }


    /**
     * Programmatic evaluation of EL.
     *
     * @param el EL to evaluate
     * @return Result of the evaluation
     */
    public static Object evaluateEL(String el) {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory =
            facesContext.getApplication().getExpressionFactory();
        ValueExpression exp =
            expressionFactory.createValueExpression(elContext, el,
                                                    Object.class);

        return exp.getValue(elContext);
    }

    /**
     * Programmatic invocation of a method that an EL evaluates to.
     * The method must not take any parameters.
     *
     * @param el EL of the method to invoke
     * @return Object that the method returns
     */
    public static Object invokeEL(String el) {

        return invokeEL(el, new Class[0], new Object[0]);
    }

    /**
     * Programmatic invocation of a method that an EL evaluates to.
     *
     * @param el EL of the method to invoke
     * @param paramTypes Array of Class defining the types of the parameters
     * @param params Array of Object defining the values of the parametrs
     * @return Object that the method returns
     */
    public static Object invokeEL(String el, Class[] paramTypes,
                                  Object[] params) {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory =
            facesContext.getApplication().getExpressionFactory();
        MethodExpression exp =
            expressionFactory.createMethodExpression(elContext, el,
                                                     Object.class, paramTypes);

        return exp.invoke(elContext, params);
    }

    public static Object invokeMethodExpressionEL(MethodExpressionImpl methodExpressionImpl,
                                                  Object[] params) {

        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();

        return methodExpressionImpl.invoke(elctx, params);
    }


    public static Object invokeMethod(String expr, Class[] paramTypes,
                                      Object[] params) {

        FacesContext fc = FacesContext.getCurrentInstance();
        MethodBinding mb;
        mb = fc.getApplication().createMethodBinding(expr, paramTypes);
        return mb.invoke(fc, params);
    }

    public static Object invokeMethod(String expr, Class paramType,
                                      Object param) {

        return invokeMethod(expr, new Class[] { paramType },
                            new Object[] { param });
    }


    public String addRowAtFirst(String iter) {

        //        CollectionModel tableModel = (CollectionModel)getEmpTable().getValue();
        //        JUCtrlHierBinding adfModel =
        //            (JUCtrlHierBinding)tableModel.getWrappedData();
        //        DCIteratorBinding dciter = adfModel.getDCIteratorBinding();
        DCIteratorBinding dciter = findIterator(iter);
        NavigatableRowIterator nav = dciter.getNavigatableRowIterator();
        Row newRow = nav.createRow();
        newRow.setNewRowState(Row.STATUS_INITIALIZED);
        nav.insertRowAtRangeIndex(0, newRow);
        dciter.setCurrentRowWithKey(newRow.getKey().toStringFormat(true));
        return null;
    }
    //To add a new row at the end of the table use this method:

    public String addRowAtLast(String iter) {

        //        CollectionModel tableModel = (CollectionModel)getEmpTable().getValue();
        //        JUCtrlHierBinding adfModel =
        //            (JUCtrlHierBinding)tableModel.getWrappedData();
        //        DCIteratorBinding dciter = adfModel.getDCIteratorBinding();
        DCIteratorBinding dciter = findIterator(iter);
        NavigatableRowIterator nav = dciter.getNavigatableRowIterator();
        Row newRow = nav.createRow();
        newRow.setNewRowState(Row.STATUS_INITIALIZED);
        Row lastRow = nav.last();
        int lastRowIndex = nav.getRangeIndexOf(lastRow);
        nav.insertRowAtRangeIndex(lastRowIndex + 1, newRow);
        dciter.setCurrentRowWithKey(newRow.getKey().toStringFormat(true));
        return null;
    }
    //To add a new row before the current selected row (default behavior) use this method:

    public String addRowBefore(String iter) {
        DCIteratorBinding dciter = findIterator(iter);
        NavigatableRowIterator nav = dciter.getNavigatableRowIterator();
        Row newRow = nav.createRow();
        newRow.setNewRowState(Row.STATUS_INITIALIZED);
        nav.insertRow(newRow);
        dciter.setCurrentRowWithKey(newRow.getKey().toStringFormat(true));
        return null;
    }

    public String addRowAfter(DCIteratorBinding dciter) {
        NavigatableRowIterator nav = dciter.getNavigatableRowIterator();
        Row newRow = nav.createRow();
        newRow.setNewRowState(Row.STATUS_INITIALIZED);
        Row currentRow = nav.getCurrentRow();
        int currentRowIndex = nav.getRangeIndexOf(currentRow);
        nav.insertRowAtRangeIndex(currentRowIndex + 1, newRow);
        dciter.setCurrentRowWithKey(newRow.getKey().toStringFormat(true));
        return null;
    }

    public void cancelResetFields(String iter) {
        ViewObject vo = ADFUtils.findIterator(iter).getViewObject();
        vo.getCurrentRow().refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
    }


    static public Application getApplication() {
        FacesContext facesContext;
        Application application;

        facesContext = FacesContext.getCurrentInstance();
        application = facesContext.getApplication();

        return application;
    }


    static public ExpressionFactory getExpressionFactory() {

        Application application;
        ExpressionFactory expressionFactory;

        application = getApplication();
        expressionFactory = application.getExpressionFactory();

        return expressionFactory;
    }


    static public DCIteratorBinding getDCIteratorBinding(String iteratorName) {

        DCBindingContainer bindings;
        DCIteratorBinding iteratorBinding;

        bindings = getDCBindingContainer();
        iteratorBinding = bindings.findIteratorBinding(iteratorName);

        return iteratorBinding;
    }

    static public String getMethodBindingString(String methodName) {

        StringBuffer methodBindingString;

        methodBindingString = new StringBuffer(100);
        methodBindingString.append("#{bindings.");
        methodBindingString.append(methodName);
        methodBindingString.append(".execute}");

        return methodBindingString.toString();
    }

    static public void addPartialTarget(UIComponent component) {

        RequestContext adfContext;

        adfContext = RequestContext.getCurrentInstance();
        adfContext.addPartialTarget(component);

    }


    static public MethodExpression getMethodExpression(String methodName,
                                                       Class returnType,
                                                       Class[] params) {

        ExpressionFactory expressionFactory;
        MethodExpression methodExpression;
        ELContext elContext;
        String methodBindingString;

        expressionFactory = getExpressionFactory();

        elContext = FacesContext.getCurrentInstance().getELContext();
        methodBindingString = getMethodBindingString(methodName);

        methodExpression =
                expressionFactory.createMethodExpression(elContext, methodBindingString,
                                                         returnType, params);


        return methodExpression;

    }

    static public Object executeMethod(String methodName, Class returnType) {
        //#{bindings.AMmethod.execute}",ReturnValue.Class,new Class[]{});
        MethodExpression methodExpression;
        ELContext elContext;
        Object value;

        methodExpression =
                getMethodExpression(methodName, returnType, new Class[] { });

        elContext = FacesContext.getCurrentInstance().getELContext();

        value = methodExpression.invoke(elContext, null);

        return value;
    }


    static public Boolean executeBooleanMethod(String methodName) {

        Object value;
        Boolean result;
        value = executeMethod(methodName, Boolean.class);

        result = Boolean.FALSE;
        if (value != null) {
            result = (Boolean)value;
        }
        return result;
    }

    static public String executeStringMethod(String methodName) {

        Object value;
        String result;
        value = executeMethod(methodName, String.class);

        result = "";
        if (value != null) {
            result = (String)value;
        }
        return result;
    }

    static public Key getSelectedTableRowKey(UIXTable table) {

        RowKeySet selected;
        List keyList;
        Key key;


        selected = table.getSelectedRowKeys();
        keyList = (List)selected.iterator().next();
        key = (Key)keyList.iterator().next();

        return key;
    }

    /**
     * Programmatic equivalent of:
     *
     *   #{bindings.PeriodVO.collectionModel.makeCurrent}
     *
     * Sets the current row to be the selected row. Coded to handle UIXTable and
     * its sub classes. Only tested with RichTable.
     *
     * or is that implied?
     *
     * @param selectionEvent
     */
    static public Object updateRowCurrency(SelectionEvent selectionEvent,
                                           String iteratorName) {

        DCIteratorBinding iterator;
        Object source;
        UIXTable table;
        Key key;
        String keyString;

        key = null;

        source = selectionEvent.getSource();

        if (source != null && source instanceof UIXTable) {

            table = (UIXTable)source;
            key = ADFUtils.getSelectedTableRowKey(table);

            if (key != null) {

                keyString = key.toStringFormat(true);

                iterator = getDCIteratorBinding(iteratorName);
                iterator.setCurrentRowWithKey(keyString);
                //                table.setRowKey(key);
            }
        }

        return key;
    }

    static public Object setPageFlowScopeValue(String name, Object value) {

        Map<String, Object> pageFlowScopeMap;

        pageFlowScopeMap =
                AdfFacesContext.getCurrentInstance().getPageFlowScope();

        value = pageFlowScopeMap.put(name, value);
        
        // FOR High Availability - this is required
        ControllerContext.getInstance().markScopeDirty(pageFlowScopeMap);
        
        return value;
    }

    static public Object getPageFlowScopeValue(String name) {

        Map<String, Object> pageFlowScopeMap;
        Object value;

        pageFlowScopeMap =
                AdfFacesContext.getCurrentInstance().getPageFlowScope();

        value = pageFlowScopeMap.get(name);

        return value;
    }

    static public Object setViewScopeValue(String name, Object value) {

        Map<String, Object> viewScopeMap;

        viewScopeMap = AdfFacesContext.getCurrentInstance().getViewScope();

        value = viewScopeMap.put(name, value);

        // FOR High Availability - this is required
        ControllerContext.getInstance().markScopeDirty(viewScopeMap);
        
        return value;
    }

    static public Object getViewScopeValue(String name) {

        Map<String, Object> viewScopeMap;
        Object value;

        viewScopeMap = AdfFacesContext.getCurrentInstance().getViewScope();

        value = viewScopeMap.get(name);

        return value;
    }

    /**
     * Method to set focus on a any arbitrary component
     * @param comp
     */
    public static void setFocus(UIComponent comp) {
        if (comp != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            StringBuilder script = new StringBuilder();
            script.append("var comp = AdfPage.PAGE.findComponent('").append(comp.getClientId(context)).append("'); ").append("comp.focus();");

            ExtendedRenderKitService erks =
                Service.getService(context.getRenderKit(),
                                   ExtendedRenderKitService.class);
            erks.addScript(context, script.toString());
        }

    }

    public static void showDialog(RichPopup popup) {
        if (popup != null) {
            /**
              * Commented and added by santosh for popup opening performance
              */
            FacesContext context = FacesContext.getCurrentInstance();
            StringBuilder script = new StringBuilder();
            script.append("var popup = AdfPage.PAGE.findComponent('").append(popup.getClientId(context)).append("'); ").append("if (!popup.isPopupVisible()) { ").append("var hints = {}; ").append("popup.show(hints);}");
            ExtendedRenderKitService erks =
                Service.getService(context.getRenderKit(),
                                   ExtendedRenderKitService.class);
            erks.addScript(context, script.toString());
        }
    }

    public static void closeDialog(RichPopup popup) {
        if (popup != null) {
            popup.hide();
        }
    }

    public static void navigateToControlFlowCase(String controlFlowCase) {

        FacesContext fctx = FacesContext.getCurrentInstance();
        Application application = fctx.getApplication();
        NavigationHandler navHandler = application.getNavigationHandler();
        navHandler.handleNavigation(fctx, null, controlFlowCase);

    }


    /**
      * @Author smishra
      * Returns the pageFlowScope(pfs) of the taskFlowId and the regionBindingname.
      *
      * Eg., There are 2 regionBindings named patientSearchFlow1 and patientSearchFlow2 in the
      * executables section. Both of then have the /WEB-INF/patient/patientSearchFlow.xml as the taskflow
      * then invoke this method to get handle of the particular region's taskflow like this:
      * getPageFlowScopeOfTaskFlowId("/WEB-INF/patient/patientSearchFlow.xml#patientSearchFlow","patientSearchFlow1")
      * getPageFlowScopeOfTaskFlowId("/WEB-INF/patient/patientSearchFlow.xml#patientSearchFlow","patientSearchFlow2"); respectively.
      *
      * Usage: Can be used in cross tab refreshing, region return parameters
      */


    /**
     * @author dsharma
     * Method used to set focus on the first row of the table
     * @param table
     */
    public static void setFirstRowSelectionForTable(UIXTable table) {

        //Adding this due to caching issue
        Object rowKey = table.getAttributes().get("scrollTopRowKey");
        RowKeySet rks = table.getSelectedRowKeys();
        if (rks != null) {
            rks.clear();
            rks.add(rowKey);
        }
    }

    public static void setSessionScopeValue(String name, Object value) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        HttpSession session = (HttpSession)ectx.getSession(false);
        session.setAttribute(name, value);
    }

    public static Object getSessionScopeValue(String name) {
        Object returnVal = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        HttpSession session = (HttpSession)ectx.getSession(false);
        returnVal = session.getAttribute(name);
        return returnVal;
    }

    public static void setRequestScopeValue(String name, Object value) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        ectx.getRequestMap().put(name, value);
    }

    public static Object getRequestScopeValue(String name) {
        Object returnVal = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext();
        returnVal = ectx.getRequestMap().get(name);
        return returnVal;
    }

    /**
     * The code below should be in a Utility class
     **/
    public static Object invokeMethodExpression(String expr, Class returnType,
                                                Class[] argTypes,
                                                Object[] args) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory =
            fc.getApplication().getExpressionFactory();
        MethodExpression methodExpr =
            elFactory.createMethodExpression(elctx, expr, returnType,
                                             argTypes);
        return methodExpr.invoke(elctx, args);
    }


    public static OperationBinding getOperBindFromPageDef(String pageDef,
                                                          String operation) {
        DCBindingContainer dc = findBindingContainerByName(pageDef);
        OperationBinding ob = null;
        if (dc != null)
            ob = dc.getOperationBinding(operation);
        return ob;
    }

    public static DCIteratorBinding getIterBindFromPageDef(String pageDef,
                                                           String iterName) {
        DCBindingContainer dc = findBindingContainerByName(pageDef);
        DCIteratorBinding iter = null;
        if (dc != null)
            iter = (DCIteratorBinding)dc.get(iterName);
        return iter;
    }

    public static DCBindingContainer findBindingContainerByName(String name) {
        return getBidingContext().findBindingContainer(name);
    }

    public static BindingContext getBidingContext() {
        return BindingContext.getCurrent();
    }

    /**
     * This method, given a parent component, will 'recurse' through its children
     * and if it finds a component that is an instance of UIXEditableValue, it will
     * perform a 'resetValue()' on that component.
     *
     * Reason for this method: When a form has fields with 'required' fields set to true,
     * then on 'clear' though the iterator is reset, the values are cached in the UI.
     * To avoid this, the callee method must reset the iterator and then call resetValueInputItems.
     *
     * @param adfFacesContext ADF Faces Context
     * @param component Parent component
     * @author Deepak Narayanan
     */
    public static void resetValueInputItems(AdfFacesContext adfFacesContext,
                                            UIComponent component) {
        if (component != null) {
            List<UIComponent> items = component.getChildren();
            for (UIComponent item : items) {
                if (item != null) {
                    resetValueInputItems(adfFacesContext, item);
                    if (item instanceof UIXEditableValue) {
                        UIXEditableValue input = (UIXEditableValue)item;
                        if (input != null) {
                            input.resetValue();
                            adfFacesContext.addPartialTarget(input);
                        }
                    }
                }
            }
        }
    }

    public static void clearMessagesForPage() {
        StringBuilder script = new StringBuilder();
        script.append("AdfPage.PAGE.clearAllMessages();");
        addJavaScript(script.toString());
    }


    public static void clearMessageForComponent(UIComponent component) {
        String script =
            String.format("function clearErrorMessage() { AdfPage.PAGE.clearMessages(\"%s\"); }; clearErrorMessage(); ",
                          component.getClientId(FacesContext.getCurrentInstance()));
        addJavaScript(script);
    }

    //-----------------------------

    /**
     * Show popup.
     *
     * @param popupId the popup id
     * @param alignId the align id
     */
    public static void showPopup(String popupId, String alignId) {
        FacesContext context = FacesContext.getCurrentInstance();

        StringBuilder script = new StringBuilder();
        script.append("var popup=AdfPage.PAGE.findComponent('").append(popupId).append("');").append("if (!popup.isPopupVisible()){").append("var hints = {}; ").append("hints[AdfRichPopup.HINT_ALIGN_ID]='").append(alignId).append("';").append("hints[AdfRichPopup.HINT_ALIGN]=AdfRichPopup.ALIGN_END_AFTER; ").append("popup.show(hints);}");

        ExtendedRenderKitService erks =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    /**
     * Show popup.
     *
     * @param popupId the popup id
     */
    public static void showPopup(String popupId) {
        FacesContext context = null;
        ExtendedRenderKitService extRenderKitSrvc = null;
        StringBuilder script = null;

        context = FacesContext.getCurrentInstance();
        extRenderKitSrvc =
                Service.getRenderKitService(context, ExtendedRenderKitService.class);
        script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('").append(popupId).append("'); ").append("popup.show();");
        extRenderKitSrvc.addScript(context, script.toString());
    }

    /**
     * Show popup.
     *
     * @param popupId the popup id
     */
    public void closePopup(String popupId) {
        FacesContext context = FacesContext.getCurrentInstance();

        ExtendedRenderKitService extRenderKitSrvc =
            Service.getRenderKitService(context,
                                        ExtendedRenderKitService.class);
        extRenderKitSrvc.addScript(context,
                                   "AdfPage.PAGE.findComponent('" + popupId +
                                   "').hide();");
    }

    /**
     * Show popup.
     *
     * @param popupId the popup id
     */
    public static void showPopup(UIComponent popupId) {
        showPopup(popupId.getClientId(getFacesContext()));
    }

    /**
     * Hide popup.
     *
     * @param popupId the popup id
     */
    public static void hidePopup(String popupId) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExtendedRenderKitService erkService =
            Service.getService(context.getRenderKit(),
                               ExtendedRenderKitService.class);
        erkService.addScript(context,
                //                         "AdfPage.PAGE.findComponent('" + popupId + "').hide();");
                "AdfPage.PAGE.findComponent('" + popupId + "').hide();");

    }

    /**
     * Hide popup.
     *
     * @param popupId the popup id
     */
    public static void hidePopup(UIComponent popupId) {
        hidePopup(popupId.getClientId(getFacesContext()));
    }

    /**
     * Gets the faces context.
     *
     * @return the faces context
     */
    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Find an operation binding in the current binding container by name.
     * @param name operation binding name
     * @return operation binding
     */
    public static OperationBinding findOperation(String name) {
        OperationBinding op =
            getDCBindingContainer().getOperationBinding(name);
        if (op == null) {
            throw new RuntimeException("Operation '" + name + "' not found");
        }
        return op;
    }

    /**
     * Will get the database connection object for the datasource JNDI name
     * @param dsContext
     * @return
     */
    public static Connection getDBConnectionFromDataSource(String dsContext) {
        //String DATASOURCE_CONTEXT = "java:comp/env/jdbc/blah";
        Connection conn = null;
        try {
            Context initialContext = new InitialContext();
            if (initialContext == null) {
                //System.out.println("JNDI problem. Cannot get InitialContext.");
            }
            DataSource datasource =
                (DataSource)initialContext.lookup(dsContext);
            if (datasource != null) {
                conn = datasource.getConnection();
            } else {
                //System.out.println("Failed to lookup datasource.");
            }
        } catch (NamingException ex) {
            //System.out.println("Cannot get connection: " + ex);
        } catch (SQLException ex) {
            //System.out.println("Cannot get connection: " + ex);
        }
        return conn;
    }

    public static void addLocalizedDetailedMessage(FacesMessage.Severity type,
                                                   String key,
                                                   String detailMsg,
                                                   UIComponent component) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        FacesMessage fm =
            new FacesMessage(type, getMessage(key, EXCEPTIONS_BUNDLE_NAME),
                             detailMsg);
        fctx.addMessage(component != null ? component.getClientId(fctx) : null,
                        fm);
    }


    /**
     * Adds the localized faces message.
     *
     * @param severity the severity
     * @param key the key
     */
    public static void addLocalizedFacesMessage(FacesMessage.Severity severity,
                                                String key,
                                                UIComponent component) {
        String message = getMessage(key, EXCEPTIONS_BUNDLE_NAME);
        FacesMessage fm = new FacesMessage(severity, message, null);
        getFacesContext().addMessage(component != null ?
                                     component.getClientId(FacesContext.getCurrentInstance()) :
                                     null, fm);
    }

    public static void addMessage(FacesMessage.Severity type, String code) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        FacesMessage fm = new FacesMessage(code);
        fm.setSeverity(type);
        fctx.addMessage(fctx.getViewRoot().getId(), fm);
    }

    /**
     * Gets the message.
     *
     * @param pKey the cle
     * @param pFile the file
     * @return the message
     */
    private static String getMessage(String pKey, String pFile) {

        String message = null;
        FacesContext messageContext;
        ResourceBundle messageBundle;

        messageContext = FacesContext.getCurrentInstance();

        try {

            if (pKey != null && pKey.trim().contains(" ")) {
                return pKey;
            }


            //logger.debug("messageContext.getViewRoot().getLocale() " + messageContext.getViewRoot().getLocale());
            messageBundle = ResourceBundle.getBundle(pFile, Locale.US);
            message = messageBundle.getString(pKey);
        }

        catch (MissingResourceException mre) {
            mre.printStackTrace();
            //logger.debug(message);
            return pKey;
        }
        return message;
    }

    public static String getContextParamValue(String param) {
        HttpServletRequest request =
            (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getSession().getServletContext().getInitParameter(param);
    }

    public static void addJavaScript(String script) {
        org.apache.myfaces.trinidad.render.ExtendedRenderKitService service =
            org.apache.myfaces.trinidad.util.Service.getRenderKitService(FacesContext.getCurrentInstance(),
                                                                         ExtendedRenderKitService.class);
        service.addScript(FacesContext.getCurrentInstance(), script);
    }


    /**
     * This methods shows the message required on the UI with required severity
     * @param msg -- Message to be shown on the UI.
     */
    public static void showFacesMessage(String msg,
                                        FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, msg, "");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public static void showFacesMessage(String msg,
                                        FacesMessage.Severity severity,
                                        UIComponent component) {
        FacesMessage message = new FacesMessage(severity, msg, "");
        FacesContext.getCurrentInstance().addMessage(component.getClientId(FacesContext.getCurrentInstance()),
                                                     message);
    }

    public static String getMessageFromBundle(String pkey, String pFile) {
        ResourceBundle rsBundle = BundleFactory.getBundle(pFile);
        String value = rsBundle.getString(pkey);
        return value;
    }

    public static XMLGregorianCalendar convertDateToXMLGregorian(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar xmlDate = null;
        try {
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            // TODO
            e.printStackTrace();
        }
        return xmlDate;
    }

    /**
     * Execute action.
     * @param pActionName the action name
     * @param pPageDefId the page def id
     * @param pParams the params
     * @return the object
     * @throws RuntimeException
     */
    public static Object executeAction(String pPageDefId, String pActionName,
                                       Map<String, Object> pParams) throws Exception {
        OperationBinding operationBinding = null;

        if (pPageDefId != null) {
            BindingContainer bc =
                BindingContext.getCurrent().findBindingContainer(pPageDefId);
            if (bc == null) {
                throw new RuntimeException((new StringBuilder()).append("No binding for ").append(pPageDefId).toString());
            }
            operationBinding = bc.getOperationBinding(pActionName);
        } else {
            operationBinding = findOperation(pActionName);
        }

        if (operationBinding == null) {
            throw new RuntimeException((new StringBuilder()).append("No binding for ").append(pActionName).toString());
        }

        if (pParams != null) {
            operationBinding.getParamsMap().putAll(pParams);
        }
        operationBinding.execute();
        //check for errors
        if (operationBinding.getErrors() != null &&
            operationBinding.getErrors().size() > 0 &&
            operationBinding.getErrors().get(0) != null) {
            Exception exception =
                (Exception)operationBinding.getErrors().get(0);
            throw exception;

        }
        Object result = operationBinding.getResult();

        return result;
    }

    /**
     * Execute action.
     * @param pActionName the action name
     * @param pParams the params
     * @return the object
     */
    public static Object executeAction(String pActionName,
                                       Map<String, Object> pParams) throws Exception {
        return executeAction(null, pActionName, pParams);
    }

    public static String getValidImageURL(String relativeUrl) {
        URL imageURL = null;
        BufferedReader in = null;
        try {
            // trying with actual url
            logger.info("getValidImageURL getRequestURLTillContextRoot:"+relativeUrl);
            imageURL = new URL(getRequestURLTillContextRoot() + relativeUrl);
          logger.info("getValidImageURL getRequestURLTillContextRoot imageURL:"+imageURL);
            in =
                new BufferedReader(new InputStreamReader(imageURL.openStream()));
          logger.info("getValidImageURL getRequestURLTillContextRoot Stream:"+in);
        } catch (IOException e) {
            // trying with localhost url
            logger.info("getValidImageURL request URL exception"+e.toString());
            try {
              logger.info("getValidImageURL getLocalHostURL "+relativeUrl);
                imageURL = new URL(getLocalHostURL() + relativeUrl);
              logger.info("getValidImageURL getLocalHostURL imageURL:"+imageURL);
                in =
                    new BufferedReader(new InputStreamReader(imageURL.openStream()));
              logger.info("getValidImageURL getLocalHostURL Stream:"+in);
            } catch (IOException ex) {
              logger.info("getValidImageURL local host exception"+ex.toString());
                e.printStackTrace();
            }
        }
        finally{
            if(in != null)
                try {
                    in.close();
                  logger.info("getValidImageURL after closing Stream:"+in);
                } catch (IOException ioe) {
                  logger.info("getValidImageURL after closing Stream exception"+ioe.toString());
                    ioe.printStackTrace();
                }
        }
     //   if (in != null)
            return imageURL.toString();
     //   else
     //       return null;
    }
    
    
    public static String getLocalHostURL() {
        HttpServletRequest request =
            (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getScheme() + "://localhost:" + request.getLocalPort() +
            request.getContextPath();
    }

    static public Timestamp getJBOTimeStamp() {
        Date date = new Date(System.currentTimeMillis());
        Timestamp timestampObj = new Timestamp(date);
        return timestampObj;
    }
}

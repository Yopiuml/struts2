/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package org.apache.struts.action2.interceptor;

import org.apache.struts.action2.ServletActionContext;
import org.apache.struts.action2.util.InvocationSessionStore;
import org.apache.struts.action2.util.TokenHelper;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.util.OgnlValueStack;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * <!-- START SNIPPET: description -->
 *
 * This interceptor builds off of the {@link TokenInterceptor}, providing advanced logic for handling invalid tokens.
 * Unlike the normal token interceptor, this interceptor will attempt to provide intelligent fail-over in the event of
 * multiple requests using the same session. That is, it will block subsequent requests until the first request is
 * complete, and then instead of returning the <i>invalid.token</i> code, it will attempt to display the same response
 * that the original, valid action invocation would have displayed if no multiple requests were submitted in the first
 * place.
 * 
 * <p/>
 * 
 * <b>NOTE:</b> As this method extends off MethodFilterInterceptor, it is capable of
 * deciding if it is applicable only to selective methods in the action class. See
 * <code>MethodFilterInterceptor</code> for more info.
 *
 * <!-- END SNIPPET: description -->
 *
 * <p/> <u>Interceptor parameters:</u>
 *
 * <!-- START SNIPPET: parameters -->
 *
 * <ul>
 *
 * <li>None</li>
 *
 * </ul>
 *
 * <!-- END SNIPPET: parameters -->
 *
 * <p/> <u>Extending the interceptor:</u>
 *
 * <p/>
 *
 * <!-- START SNIPPET: extending -->
 *
 * There are no known extension points for this interceptor.
 *
 * <!-- END SNIPPET: extending -->
 *
 * <p/> <u>Example code:</u>
 *
 * <pre>
 * <!-- START SNIPPET: example -->
 * 
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *     &lt;interceptor-ref name="token-session/&gt;
 *     &lt;interceptor-ref name="basicStack"/&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * 
 * &lt;-- In this case, myMethod of the action class will not 
 *        get checked for invalidity of token --&gt;
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *     &lt;interceptor-ref name="token-session&gt;
 *         &lt;param name="excludeMethods"&gt;myMethod&lt;/param&gt;
 *     &lt;/interceptor-ref name="token-session&gt;
 *     &lt;interceptor-ref name="basicStack"/&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * 
 * <!-- END SNIPPET: example -->
 * </pre>
 *
 * @author Jason Carreira
 * @author Rainer Hermanns
 * @author Nils-Helge Garli
 * 
 * @version $Date: 2006/03/18 04:45:13 $ $Id: TokenSessionStoreInterceptor.java,v 1.17 2006/03/18 04:45:13 tmjee Exp $
 */
public class TokenSessionStoreInterceptor extends TokenInterceptor {
    protected String handleInvalidToken(ActionInvocation invocation) throws Exception {
        ActionContext ac = invocation.getInvocationContext();

        HttpServletRequest request = (HttpServletRequest) ac.get(ServletActionContext.HTTP_REQUEST);
        String tokenName = TokenHelper.getTokenName();
        String token = TokenHelper.getToken(tokenName);

        Map params = ac.getParameters();
        params.remove(tokenName);
        params.remove(TokenHelper.TOKEN_NAME_FIELD);

        if ((tokenName != null) && (token != null)) {
            ActionInvocation savedInvocation = InvocationSessionStore.loadInvocation(tokenName, token);

            if (savedInvocation != null) {
                // set the valuestack to the request scope
                OgnlValueStack stack = savedInvocation.getStack();
                request.setAttribute(ServletActionContext.STRUTS_VALUESTACK_KEY, stack);

                Result result = savedInvocation.getResult();

                if ((result != null) && (savedInvocation.getProxy().getExecuteResult())) {
                    result.execute(savedInvocation);
                }

                // turn off execution of this invocations result
                invocation.getProxy().setExecuteResult(false);

                return savedInvocation.getResultCode();
            }
        }

        return INVALID_TOKEN_CODE;
    }

    protected String handleValidToken(ActionInvocation invocation) throws Exception {
        // we know the token name and token must be there
        ActionContext ac = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) ac.get(ServletActionContext.HTTP_REQUEST);
        String key = TokenHelper.getTokenName();
        String token = TokenHelper.getToken(key);
        InvocationSessionStore.storeInvocation(key, token, invocation);

        return invocation.invoke();
    }
}

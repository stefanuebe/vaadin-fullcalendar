package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.dom.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the client calls queued by {@link Element#callJsFunction(String, Object...)} the same way on every supported
 * Vaadin version.
 * <p>
 * Up to Vaadin 25.3, a call is sent as an expression that names the function, {@code return $0.name($1,$2)}, and its
 * parameters are the element followed by the arguments. Since Vaadin 25.4 (vaadin/flow#25852) every call shares one
 * expression, and its parameters are the function name, the arguments and the element.
 */
final class ClientCalls {

    private static final Pattern NAMED_CALL = Pattern.compile("^return \\$0\\.([\\w$.]+)\\(");

    private ClientCalls() {
    }

    /**
     * Returns the name of the client function the invocation calls, or {@code null} if it is not a
     * {@code callJsFunction} call, for example a script run with {@code executeJs}.
     */
    static String functionName(PendingJavaScriptInvocation invocation) {
        String expression = invocation.getInvocation().getExpression();
        Matcher named = NAMED_CALL.matcher(expression);
        if (named.find()) {
            return named.group(1);
        }
        List<Object> parameters = invocation.getInvocation().getParameters();
        if (expression.contains("$0.split('.')") && parameters.size() >= 2
                && parameters.getFirst() instanceof String name && parameters.getLast() instanceof Element) {
            return name;
        }
        return null;
    }

    /** Returns whether the invocation calls the named client function. */
    static boolean calls(PendingJavaScriptInvocation invocation, String function) {
        return function.equals(functionName(invocation));
    }

    /** Returns the arguments passed to the client function, without the element and the function name. */
    static List<Object> arguments(PendingJavaScriptInvocation invocation) {
        List<Object> parameters = invocation.getInvocation().getParameters();
        boolean nameFirst = parameters.getFirst() instanceof String;
        return parameters.subList(1, nameFirst ? parameters.size() - 1 : parameters.size());
    }

    /** Returns the last argument passed to the client function. */
    static Object lastArgument(PendingJavaScriptInvocation invocation) {
        return arguments(invocation).getLast();
    }
}

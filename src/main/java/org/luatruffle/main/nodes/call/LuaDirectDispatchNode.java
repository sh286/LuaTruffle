package org.luatruffle.main.nodes.call;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import org.luatruffle.main.runtime.LuaFunction;

/**
 * Created by Lucas Allan Amorim on 2014-09-24.
 */
public final class LuaDirectDispatchNode extends LuaAbstractDispatchNode {

    private final LuaFunction cachedMethod;
    @Child private DirectCallNode callCachedTargetNode;
    private final Assumption cachedTargetStable;
    @Child private LuaAbstractDispatchNode nextNode;

    public LuaDirectDispatchNode(LuaAbstractDispatchNode next, LuaFunction cachedFunction) {
        this.cachedMethod = cachedFunction;
        this.callCachedTargetNode = Truffle.getRuntime().createDirectCallNode(cachedFunction.getCallTarget());
        this.cachedTargetStable = cachedFunction.getCallTargetStable();
        this.nextNode = next;
    }

    @Override
    protected Object executeDispatch(VirtualFrame frame, LuaFunction function, Object[] arguments) {
        if (this.cachedMethod == function) {
            try {
                cachedTargetStable.check();
                return callCachedTargetNode.call(frame, arguments);
            } catch (InvalidAssumptionException ex) {
          replace(nextNode);
            }
        }
        return nextNode.executeDispatch(frame, function, arguments);
    }
}

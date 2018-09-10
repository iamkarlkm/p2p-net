package javax.net.p2p.api;

/**
 * 自定义P2P协议命令
 *
 */
public enum P2PCommand {
    /**
     * 系统消息
     */
    SYSTEM(1),
    /**
     * 登录指令
     */
    LOGIN(2),
    /**
     * 登出指令
     */
    LOGOUT(3),
    /**
     * 握手消息
     */
    HAND(4),
    /**
     * 查询节点信息
     */
    GET_NODE(5),
    /**
     * 查询节点信息
     */
    STD_RESPONSE(6);

    private final int value;

    P2PCommand(int val) {
        this.value = val;
    }

    public int getValue() {
        return value;
    }

}

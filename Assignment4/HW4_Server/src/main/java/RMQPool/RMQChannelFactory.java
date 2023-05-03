package RMQPool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The type Rmq channel factory.
 *
 * @className: RMQChannelFactory
 * @author: Bingfan Tian
 * @description: TODO
 * @date: 10 /22/22 5:06 PM
 */
public class RMQChannelFactory extends BasePooledObjectFactory<Channel>{

    private int count;
    private final Connection connection;

    /**
     * Instantiates a new Rmq channel factory.
     *
     */
    public RMQChannelFactory(Connection connection) throws IOException, TimeoutException {
        this.connection = connection;
        this.count = 0;
    }

    @Override
    synchronized public Channel create() throws Exception {
        count++;
        return this.connection.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }

    /**
     * Gets channel count.
     *
     * @return the channel count
     */
    public int getChannelCount() {
        return count;
    }
}

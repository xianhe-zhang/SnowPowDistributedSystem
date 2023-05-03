import com.rabbitmq.client.*;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ConnectionPoolFactory extends BasePooledObjectFactory<Channel> {
  ConnectionFactory factory = new ConnectionFactory();

  @Override
  public Channel create() throws Exception {

    if (Constant.test_local) {
      factory.setHost("localhost");
      factory.setPort(5672);
      factory.setUsername("guest");
      factory.setPassword("guest");
    } else{
      factory.setHost("35.92.110.245");
      factory.setPort(5672);
      factory.setUsername("mario");
      factory.setPassword("mariobar");
    }

    Connection connection = factory.newConnection();
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }


  @Override
  public void destroyObject(PooledObject<Channel> p) throws Exception {
    p.getObject().close();
  }
}

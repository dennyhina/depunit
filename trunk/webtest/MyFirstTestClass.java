import org.depunit.annotations.*;
import static org.junit.Assert.*;

public class MyFirstTestClass
  {
  @Test
  public void testOne()
    {
    System.out.println("Test one, hurray!");
    assertTrue(true);
    }
  }

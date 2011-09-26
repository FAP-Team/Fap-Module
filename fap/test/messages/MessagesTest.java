package messages;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.oval.constraint.AssertFalse;

public class MessagesTest {

	
	@Test
	public void noMessages(){
		assertFalse(Messages.hasMessages());
	}

	@Test
	public void addMessages(){
		Messages.error("mensaje de error");
		assertTrue(Messages.hasMessages());
	}

}

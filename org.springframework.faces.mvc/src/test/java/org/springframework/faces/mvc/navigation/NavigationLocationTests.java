package org.springframework.faces.mvc.navigation;

import junit.framework.TestCase;

import org.springframework.util.ObjectUtils;

public class NavigationLocationTests extends TestCase {

	public void testNavigationLocationConstructorAndGets() throws Exception {

		NavigationLocation navigationLocation = new NavigationLocation("location");
		assertEquals("location", navigationLocation.getLocation());
		assertFalse(navigationLocation.isPopup());
		assertTrue(navigationLocation.getFragments().length == 0);

		navigationLocation = new NavigationLocation("location", true, new String[] { "fragment" });
		assertEquals("location", navigationLocation.getLocation());
		assertTrue(navigationLocation.isPopup());
		assertTrue(navigationLocation.getFragments().length == 1);
		assertEquals("fragment", navigationLocation.getFragments()[0]);

		navigationLocation = new NavigationLocation("location", true, null);
		assertNotNull(navigationLocation.getFragments());
	}

	public void testToString() throws Exception {
		NavigationLocation navigationLocation = new NavigationLocation("location", true, new String[] { "fragment" });
		String id = ObjectUtils.getIdentityHexString(navigationLocation);
		assertEquals("[NavigationLocation@" + id
				+ " location = 'location', popup = true, fragments = array<String>['fragment']]", navigationLocation
				.toString());
	}

	public void testHashCodeAndEquals() throws Exception {

		NavigationLocation n1 = new NavigationLocation("l1");
		NavigationLocation n2 = new NavigationLocation("l1");
		NavigationLocation n3 = new NavigationLocation("l1", false, null);
		NavigationLocation n4 = new NavigationLocation("l1", false, null);
		NavigationLocation n5 = new NavigationLocation("l1", true, null);
		NavigationLocation n6 = new NavigationLocation("l1", true, null);
		NavigationLocation n7 = new NavigationLocation("l1", true, new String[] { "test" });
		NavigationLocation n8 = new NavigationLocation("l1", true, new String[] { "test" });
		NavigationLocation n9 = new NavigationLocation("l1", true, new String[] { "test", "test" });

		assertEquals(n1.hashCode(), n1.hashCode());
		assertEquals(n1.hashCode(), n2.hashCode());
		assertEquals(n3.hashCode(), n4.hashCode());
		assertEquals(n5.hashCode(), n6.hashCode());
		assertEquals(n7.hashCode(), n8.hashCode());

		assertEquals(n1, n1);
		assertEquals(n1, n2);
		assertEquals(n1, n3);
		assertEquals(n3, n4);
		assertEquals(n5, n6);
		assertEquals(n7, n8);

		assertFalse(n1.equals("test"));
		assertFalse(n1.equals(null));
		assertFalse(n1.equals(n5));
		assertFalse(n1.equals(n9));
		assertFalse(n3.equals(n5));
		assertFalse(n5.equals(n7));
		assertFalse(n5.equals(n9));
		assertFalse(n8.equals(n9));
	}
}

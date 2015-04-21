# Cleanup Method #

Cleanup methods are declared like this:
```
@Test(cleanupMethod = "logout")
public void login()
...

@Test
public void logout()
...
```

Under the covers the cleanup method acts like a hard dependency.  In the case above logout will always run after login if and only if login succeeds.  The difference from a normal hard dependency is that the cleanup method will run after all other hard dependencies of login have ran.

When used with a DataDriver the cleanup method is called for each iteration of the driver.


## Cleanup methods and hard dependencies ##

As noted above the cleanup method will run immediately after all other hard dependencies have ran.
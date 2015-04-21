DepUnit is designed with the belief that in any complicated system the unit tests build upon one another.  In many unit test frame works there is a considerable amount of effort put into allowing the tester to setup and tear down the environment so that the tests can be performed.  We believe that this setup and tear down procedure should be a unit test as well.  For example a connection needs to be made to the database before unit tests can be performed on the data.  The connection process should be a unit test and the other tests depend on it to succeed.

With this in mind DepUnit lets you create what we call hard and soft [dependencies](Dependencies.md) between tests.  DepUnit also allows for clean up tests that perform the tear down of other frameworks.

DepUnit is also designed with the idea that data is at the heart of all unit tests.  Unit tests usually test a system with some given set of data.  DepUnit provides easy mechanisms for passing data into unit tests and passing data from one test to another.  DepUnit also provides an easy way to run the same tests multiple times with different data.

Try the GettingStarted page to go through a quick tutorial on using DepUnit.

[Main](Main.md) - Reasons for yet another unit test framework.

I'm still trying to figure out a good way to present the end user with a good navigation menu.  Until I come up with something better please have a look a the pages in under the Wiki tab above.
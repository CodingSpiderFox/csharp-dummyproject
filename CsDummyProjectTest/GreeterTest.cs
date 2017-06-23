using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CsDummyProject;
using NUnit.Framework;

namespace CsDummyProjectTest
{
    [TestFixture]
    public class GreeterTest
    {
        [Test]
        public void GetGreetingTest()
        {
            String result = Greeter.GetGreeting();
            Assert.AreEqual("Hello World", result);
        }
    }
}

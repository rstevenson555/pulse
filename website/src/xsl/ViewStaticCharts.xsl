<?xml version="1.0"?>



<xsl:stylesheet version="1.0"

 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">



<xsl:import href="ARTPage.xsl"    />



  <xsl:template match="DashBoard">

   <a href="ViewDailySessionSummary.web" title="Return to home page" accesskey="1"><img src="images/ekg.gif" width="209" style="height:155px;" alt="Pulse logo"/></a>

    <div id="tools">

        <ul id="mainNav" style="display:none;">

            <li><a href="download.html" title="Real time charts of boiseoffice.com performance">Production Environment</a></li>

            <li><a href="products/" title="Historical charst of boiseoffice.com performance">Test Environment</a></li>

            <li><a href="support/" title="Report a bug">Development Environment</a></li>

        </ul>



        <div id="sf" style="height:155px;display:none">

            <div id="realtimegraphs">

                <div id="chatdiv">


                </div>

            </div>

        </div>

     </div>

  </xsl:template>



  <xsl:template match="Body">

    <hr class="hide"/>

 

    <div id="mainContent" style="margin-top: -170px;position: relative;">

        <div id="which">



        <h2>Average LoadTime Performance</h2>

        <dl>

            <dt class="im"/>

            <dd>



            <ul id="ftr">

                <p class="d1">The following are realtime graphs of the Average Page Load Time.</p>

                <img src="testchart.web?classification=7701&amp;start=20040706090000&amp;end=20040706150000" alt="Greetings There" height="225" width="700" />

                <img src="testchart.web?classification=7702&amp;start=20040706090000&amp;end=20040706150000" alt="Greetings There" height="225" width="700" />

                <img src="testchart.web?classification=7703&amp;start=20040706090000&amp;end=20040706150000" alt="Greetings There" height="225" width="700" />

                <img src="testchart.web?classification=7704&amp;start=20040706090000&amp;end=20040706150000" alt="Greetings There" height="225" width="700" />


            </ul>



            <div style="clear: both"></div>

            </dd>

        </dl>





        <h2>AS400 Connectivity Performance</h2>

        <dl>

            <ul id="ftr">

                <p class="d1">The following are realtime graphs of the Average Page Load Time.</p>


            </ul>

       

        </dl>



        <h2>Contact us on issues identified in ART</h2>

        <dl class="footerbg">

            <dt class="im">

                <a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">

                    <img src="images/ico-bugz.gif" width="34" height="34" alt="Bugzilla"/>

                </a>

            </dt>

            <dt><a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">Bugzilla</a></dt>

            <dd>



                <p>Enterprise-grade bug tracking software [<a href="http://itatfs01pc:7000/bugzilla/enter_bug.cgi">more</a>]</p>

            </dd>

        </dl>

    </div>

    <!-- closes #which -->

  

    <hr class="hide"/>

    <div id="footer">



        <ul id="bn">

            <li><a href="sitemap.html">Site Map</a></li>

            <li><a href="contact/">Contact Us</a></li>

            <li><a href="foundation/donate.html">About ART</a></li>

            <li><a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">Install Java</a></li>

        </ul>

        <p>Copyright ; 1998-2003 BoiseOffice Solutions</p>

    </div>



    </div>

    <!-- closes #mainContent-->

  </xsl:template>



  <xsl:template match="LeftPanel">

    <div id="side">

        <h2>Other Graphs</h2>

        <p>Navigate to other graphs and information...</p>



    <ul id="oN">

        <li>

            <a href="ViewRealTimeCharts.web">Realtime Charts</a>

        </li>

        <li>

            <a href="ViewHistoricalCharts.web">Financial Summary</a>

        </li>

        <li>

            <a href="ViewDailySessionSummary.web">Daily Performance Stats</a>

        </li>

        <li>

            <a href="ViewDailyPageLoadCharts.web">Performance Details </a>

        </li>

        <li>

            <a href="http://java.sun.com/webapps/getjava/BrowserRedirect?locale=en&amp;host=www.java.com:80">Install Java</a>

        </li>

    </ul>

    

    </div>

    <!-- closes #side -->

  </xsl:template>





</xsl:stylesheet>


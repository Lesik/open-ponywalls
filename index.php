<!doctype html>
<html lang="en">
  <head>
  <meta charset="utf-8">
    <link type="image/x-icon" rel="icon" href="../../images/favicons/cat.ico">
    <link rel="stylesheet" href="pipe.css">
    <title>PonyWalls</title>
  </head>
  <body id="front">
    <div id="intro">
        <img id="logo" src="../../images/ponywalls-logo.png" alt="logo">

        <p>Ponywalls is a lorem ipsum dolem <a
        href="http://code.google.com/p/v8/">Chrome's JavaScript runtime</a>
        for easily building fast, scalable network applications.  Node.js
        uses an event-driven, non-blocking I/O model that makes it
        lightweight and efficient, perfect for data-inten...</p>

        <p>Current Version: Pre-Alpha (no release)</p>

        <div class=buttons>
        <a href="http://nodejs.org/dist/v0.10.21/node-v0.10.21.tar.gz" class="button downloadbutton" id="downloadbutton">INSTALL</a>

        <a href="download/" class=button id="all-dl-options">Downloads</a><a href="api/" class="button" id="docsbutton">Source</a>
        </div>

        <!--a href="http://github.com/joyent/node"><img class="forkme" src="http://nodejs.org/images/forkme.png" alt="Fork me on GitHub"></a-->
    </div>

    <div id="quotes" class="clearfix">
		<h2>The developer would like to thank:</h2>
		<ul>
			<li>
				<img src="http://www.android.com/images/logo.png" alt="logo" style="margin-top:11px; margin-bottom:1px;" />
				<p>The <a href="https://android.com/" target="new">Android Open Source Project</a>.
			</li>
			<li>
				<!--img src="http://actionbarsherlock.com/static/mascot.png" alt="logo" width="30" /-->
				<img src="http://actionbarsherlock.com/static/logo.png" alt="logo" width="175" style="margin-top:4px; margin-bottom:-6px;" />
				<p><a href="http://serverfire.net" target="new">ActionBarSherlock</a> allows us to support pre-Honeycomb devices.</p>
			</li>
			<li>
				<img src="../../images/elementary-logo.png" alt="logo" height="28" style="margin-top:6px; margin-bottom:-4px;" />
				<p>The icon is from <a href="http://elementaryos.org/" target="new">Elementary</a>.</p>
			</li>
			<li>
				<img src="../../images/nodejs-logo.png" alt="logo" height="30" />
				<p>Beautiful webdesign by <a href="http://nodejs.org/" target="new">node.js.</a></p>
			</li>
		</ul>
		<!--h2 style="clear:both">
			<a href="/industry/" target="new">More...</a>
		</h2-->
	</div>

    <div id="content" class="clearfix">
            <div id="column1">
                <h2>An example: Webserver</h2>
                <p>This simple web server written in Node responds with "Hello World" for every request.</p>
              <pre>
var http = require('http');
http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'text/plain'});
  res.end('Hello World\n');
}).listen(1337, '127.0.0.1');
console.log('Server running at http://127.0.0.1:1337/');</pre>

              <p>To run the server, put the code into a file
              <code>example.js</code> and execute it with the
              <code>node</code> program from the command line:</p>
              <pre class="sh_none">
% node example.js
Server running at http://127.0.0.1:1337/</pre>

                <p>Here is an example of a simple TCP server which listens on port 1337 and echoes whatever you send it:</p>

                <pre>
var net = require('net');

var server = net.createServer(function (socket) {
  socket.write('Echo server\r\n');
  socket.pipe(socket);
});

server.listen(1337, '127.0.0.1');</pre>

                <!-- <p>Ready to dig in? <a href="">Download the latest version</a> of node.js or learn how other organizations are <a href="">using the technology</a>.</p> -->
        </div>
        <div id="column2">
            <!--h2>Featured</h2-->
            <img src="../../images/nexus4.png" style="margin-top:20px">
			<!--A guided introduction to Node>

            <h2>Explore Node.js</h2>
            <ul id="explore">
                <li><a href="about/" class="explore">About</a><br><span>Technical overview</span></li>
                <li><a href="http://npmjs.org/" class="explore">npm Registry</a><br><span>Modules, resources and more</span></li>
                <li><a href="http://nodejs.org/api/" class="explore">Documentation</a><br><span>API Specifications</span></li>
                <li><a href="http://blog.nodejs.org" class="explore">Node.js Blog</a><br><span>Insight, perspective and events</span></li>
                <li><a href="community/" class="explore">Community</a><br><span>Mailing lists, blogs, and more</span></li>
                <li><a href="logos/" class="explore">Logos</a><br><span>Logo and desktop background</span></li>
                <li><a href="http://jobs.nodejs.org/" class="explore">Jobs</a><br><ol class="jobs"><li><a href='http://jobs.nodejs.org/a/jbb/redirect/963492'>Scribd</a></li><li><a href='http://jobs.nodejs.org/a/jbb/redirect/962898'>creativeLIVE</a></li></ol></li>
            </ul-->
    </div>
</div>

    <div id="footer">
        <!--a href="http://joyent.com" class="joyent-logo">Joyent</a-->
        <ul class="clearfix">
            <!--li><a href="/">Node.js</a></li>
            <li><a href="/download/">Download</a></li>
            <li><a href="/about/">About</a></li>
            <li><a href="http://npmjs.org/">npm Registry</a></li>
            <li><a href="http://nodejs.org/api/">Docs</a></li>
            <li><a href="http://blog.nodejs.org">Blog</a></li>
            <li><a href="/community/">Community</a></li-->
        </ul>

			<p><span style="-moz-transform: scaleX(-1); -o-transform: scaleX(-1); -webkit-transform: scaleX(-1); transform: scaleX(-1); display: inline-block;">&copy;</span> Copyleft 2013 Lesik P. ("K&aelig;de")&nbsp;-&nbsp;<a href="https://www.gnu.org/copyleft/copyleft.en.html" target="new">https://www.gnu.org/copyleft/copyleft.en.html</a></p>
		</div>
	</body>
</html>
SwingJS distribution -- libjs directory

This directory can be used to hold pre-compiled *.zip files that 
have root directories swingjs and swingjs/j2s. They will be 
unzipped into the site/ directory by build-site.xml. 

Generally these files would be derived from .jar files that your 
program uses. If your program has jar dependencies, you will need to 
find the source for those, compile it, and then zip the resulting 
.js files into xxxx.zip. The ANT task that might do this should look
something like this:

	<target name="zipjson">
		  <!-- org.json.simple -->
		    <property name="json.zip" value="${libjs.dir}/jsonsimple-site.zip" />	  	  	
		  	<echo> Zipping up ${json.zip} </echo>
		  	<zip destfile="${json.zip}" basedir="${site.dir}" includes="swingjs/j2s/org/json/**" />
	</target>


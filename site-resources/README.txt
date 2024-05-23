SwingJS distribution: site-resources directory

This directory can be used to hold files that programs within this Eclipse project 
need to run -- data files or images embedded within the site's html pages, for instance. Files in the site-resources directory are usually copied to the site by an ant script, such as build-site.xml.

The default site is the Eclipse project's ./site directory.

Note that the SwingJS JavaScript library is a subdirectory within the site. JavaScript library resources should be placed in the project's ./resources directory, not the site-resources directory.
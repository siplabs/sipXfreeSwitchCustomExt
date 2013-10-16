include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/config/sipxconfig.am

SUBDIRS = \
	. \
	test

EXTRA_DIST = \
	plugin \
	src

jardir = @SIPX_JAVADIR@/sipXconfig/plugins
JAR_FILE = freeswitchcustomextensions.jar
jar_DATA = $(JAR_FILE)

freeswitchcustomextensions_SRC = $(shell cd $(srcdir); find src -type f -name '*.java')
freeswitchcustomextensions_RESOURCES = $(shell cd $(srcdir)/src; find -type f -not -name '*.java')

plugin_RESOURCES = \
	$(shell cd $(srcdir); find plugin -type f \( \
	-name '*.html' \
	-o -name '*.page' \
	-o -name '*.properties' \
	-o -name '*.script' \
	-o -name '*.js' \
	-o -name '*.css' \
	\))

freeswitchcustomextensions_PKGS = \
	sipxconfig-neoconf \
	sipxconfig-web \
	$(sipxconfig_web_PKGS)

freeswitchcustomextensions_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons @SIPX_JAVADIR@/sipXconfig,$(freeswitchcustomextensions_PKGS))

$(JAR_FILE) : javac-freeswitchcustomextensions $(addprefix src/,$(freeswitchcustomextensions_RESOURCES))
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(plugin_RESOURCES)) \
	  $(call JarInclude,$(srcdir)/src,$(freeswitchcustomextensions_RESOURCES))

# style checks java source code coding style agains proper sipxecs conforming. Not nec.
# but you will probably find very useful
style_SRC = $(freeswitchcustomextensions_SRC)

precommit : style validate;

validate : src/sipxplugin.beans.xml
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans-2.0.xsd  \
	$^

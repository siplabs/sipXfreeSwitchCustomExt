include $(top_srcdir)/config/utility.am

confsubdir = freeswitchcustomextensions

confdir = $(SIPX_CONFDIR)/$(confsubdir)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	00_sipxcustomfse.cf

dbpatchdir = $(SIPX_CONFDIR)/database/plugin.d/v12
dist_dbpatch_DATA = \
	freeswitchcustomextensions.sql

xml = \
	$(confsubdir)/CustomFreeswitchExtensionsSettings.xml

dist_conf_DATA = \
	$(xml) \
	$(confsubdir)/CustomFreeswitchExtensionsSettings.properties

fsdir = $(SIPX_CONFDIR)/freeswitch
dist_fs_DATA = \
	freeswitch/sipXcfse_context.xml.vm

precommit : validate-xml

validate-xml : $(xml)
	xmllint --noout $^



# Control build verbosity
#
#  V=1,2: Enable echo of commands
#  V=2:   Enable bug/verbose options in tools and scripts

ifeq ($(V),1)
export Q :=
else
ifeq ($(V),2)
export Q :=
BUILDOPT := "VERBOSE=1"
else
export Q := @
endif
endif

define CD
	$(Q) $(eval _D=$(firstword $(1) $(@D)))
	$(Q) cd $(_D)
endef

define DELFILE
	@echo DEL: $(1)
	$(Q) rm -rf $(1)
endef

define INSTALL
	$(eval _target := $(1))
	@echo INSTALL: $(notdir $(_target))
	$(Q) cp -f $(_target) $(2)
endef

CORES := $(shell getconf _NPROCESSORS_ONLN)

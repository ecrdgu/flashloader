

# Control build verbosity
#
#  V=1,2: Enable echo of commands
#  V=2:   Enable bug/verbose options in tools and scripts

ifeq ($(V),1)
export Q :=
else
ifeq ($(V),2)
export Q :=
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

CFLAGS += -O2 -pipe
CORES := $(shell getconf _NPROCESSORS_ONLN)

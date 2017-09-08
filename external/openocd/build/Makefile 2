include define.mk

WD = $(abspath $(shell pwd))

PREFIXDIR := $(WD)/..
SRCDIR := $(WD)/libs

CHOST ?= $(shell gcc -dumpmachine)
CTARGET ?= $(shell gcc -dumpmachine)
CBUILD ?= $(shell gcc -dumpmachine)

define download
	$(Q) $(MAKE) -C $(SRCDIR) \
		url="$(url)" version="$(version)" pkg="$(@)" download
endef

define build
	$(call download)
	$(Q) $(MAKE) -C $(SRCDIR) -f Makefile \
		TOOL="$(TOOL)" PREFIXDIR="$(PREFIXDIR)" \
		CHOST="$(CHOST)" CTARGET="$(CTARGET)" CBUILD="$(CBUILD)" $(@)
endef

ifneq ($(findstring msys,$(CTARGET))$(findstring mingw,$(CTARGET)),)
TOOL ?= win
else
ifneq ($(findstring linux,$(CTARGET)),)
TOOL ?= linux
else
ifneq ($(findstring darwin,$(CTARGET)),)
TOOL ?= macos
else
TOOL :=
endif
endif
endif

ifneq ($(findstring x86_64,$(CTARGET)),)
ARCH = 64
else
ARCH = 32
endif

#SUBDIRS := $(patsubst %/,%, $(notdir $(wildcard $(BUILDDIR)/*)))
#CLEANDIR := $(patsubst %/,%,$(dir $(wildcard build/*/Makefile)))

all: openocd
.PHONY: context libusb libusb-compat-0.1 libconfuse libftdi hidapi openocd

#$(foreach SDIR, $(CLEANDIR), $(eval $(call SDIR_target,$(SDIR),clean)))
context:
ifeq ($(shell which $(CHOST)-gcc),)
	$(eval CHOST = )
endif
	@echo CHECK: $(@)
	@echo HOST: $(CHOST)
	@echo TARGET: $(CTARGET)
	@echo BUILD: $(CBUILD)
	$(Q) ./check.sh || exit 1;

libusb: context
	$(eval url := https://github.com/libusb/libusb.git)
	$(eval version := v1.0.21-rc6)
	$(call build)

libusb-compat-0.1: context libusb
	$(eval url := https://github.com/libusb/libusb-compat-0.1.git)
	$(eval version := v0.1.6-rc2)
	$(call build)

libftdi: context libusb libconfuse
	$(eval url := http://repo.or.cz/libftdi.git)
	$(eval version := v1.4rc2)
	$(call build)

libconfuse: context
	$(eval url := https://github.com/martinh/libconfuse.git)
	$(eval version := v3.2.1)
	$(call build)

hidapi: context libusb
	$(eval url := https://github.com/signal11/hidapi.git)
	$(eval version := hidapi-0.8.0-rc1)
	$(call build)

openocd: context libusb libusb-compat-0.1 libftdi hidapi
	$(eval url := http://repo.or.cz/openocd.git)
	$(eval version := v0.10.0)
	$(call build)

clean:
	@echo "CLEAN.."
	$(Q) $(MAKE) -C $(SRCDIR) $(@)

#
# Make install targets
#

define .name_tool
	$(eval prefix = @executable_path)
	$(eval deps := $(shell otool -L $(1) | awk '{print $$1}' | grep dylib))
	$(Q) for dep in $(deps); do \
		if [ "`dirname $$dep`" == "$(PREFIXDIR)/lib" ] || [ "$$dep" == "libftdi1.2$(LIBEXT)" ]; then \
			install_name_tool -change $$dep "$(prefix)/`basename $$dep`" $(1); \
		fi \
	done;
endef

.install_macos:
	$(eval IDIR := $(PREFIXDIR)/$(TOOL))
	$(eval LIBEXT = .dylib)

	$(eval FILES += $(PREFIXDIR)/lib/libftdi1.2.4.0$(LIBEXT))
	$(eval FILES += $(PREFIXDIR)/lib/libusb-1.0.0$(LIBEXT))
	$(eval FILES += $(PREFIXDIR)/lib/libusb-0.1.4$(LIBEXT))
	$(eval FILES += $(PREFIXDIR)/lib/libhidapi.0$(LIBEXT))
	$(eval FILES += $(PREFIXDIR)/bin/openocd$(EXT))	
	
	$(foreach f, $(FILES), $(call .name_tool, $(f)))

	$(Q) if [ ! -e $(IDIR) ]; then mkdir -p $(IDIR); fi
	$(Q) if [ ! -e $(IDIR)/libftdi1.2$(LIBEXT) ]; then \
		ln -s $(PREFIXDIR)/lib/libftdi1.2.4.0$(LIBEXT)  $(IDIR)/libftdi1.2$(LIBEXT); \
	fi

.install_linux:
	$(eval IDIR := $(PREFIXDIR)/$(TOOL)$(ARCH))
	$(eval LIBEXT = .so)
	$(eval FILES += $(PREFIXDIR)/bin/openocd$(EXT))

.install_win:
	$(eval IDIR := $(PREFIXDIR)/$(TOOL)$(ARCH))
	$(eval LIBEXT = .dll.a)
	$(eval EXT = exe)
	$(eval FILES += $(PREFIXDIR)/bin/openocd$(EXT))	
	$(eval FILES += $(PREFIXDIR)/lib/libusb-1.0.dll)

install: .install_$(TOOL)
ifeq ("$(wildcard $(IDIR))","")
	@echo IDIR: $(IDIR)
	$(Q) if [ ! -e $(IDIR) ]; then mkdir -p $(IDIR); fi
endif
	$(Q) cp -f $(FILES) $(IDIR)/
	$(call DELFILE, $(PREFIXDIR)/lib $(PREFIXDIR)/include $(PREFIXDIR)/bin)

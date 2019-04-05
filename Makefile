GIT = git
MAKE = make
DOCKERREGISTRY = drbd.io
DOCKERREGPATH_CONTROLLER = $(DOCKERREGISTRY)/linstor-controller
DOCKERREGPATH_SATELLITE = $(DOCKERREGISTRY)/linstor-satellite
DOCKERFILE_CONTROLLER = Dockerfile.controller
DOCKERFILE_SATELLITE = Dockerfile.satellite
DOCKER_TAG ?= latest

GENRES=./server/generated-resources
GENSRC=./server/generated-src
VERSINFO=$(GENRES)/version-info.properties

# echo v0.1 to get it started
VERSION := $(shell echo $(shell git describe --tags || echo "v0.1") | sed -e 's/^v//;s/^[^0-9]*//;s/-/./;s/\(.*\)-g/\1-/')
GITHASH := $(shell git rev-parse HEAD)

.PHONY: .filelist
.filelist:
	@set -e ; submodules=`$(GIT) submodule foreach --quiet 'echo $$path'`; \
		$(GIT) ls-files | \
		grep -vxF -e "$$submodules" | \
		sed '$(if $(PRESERVE_DEBIAN),,/^debian/d)' | \
		grep -v "gitignore\|gitmodules" > .filelist
	@$(GIT) submodule foreach --quiet 'git ls-files | sed -e "s,^,$$path/,"' | \
		grep -v "gitignore\|gitmodules" >> .filelist
	@[ -s .filelist ] # assert there is something in .filelist now
	@echo $(VERSINFO) >> .filelist
	@find $(GENSRC) -name '*.java' >> .filelist
	@echo libs >> .filelist
	@echo server/jar.deps >> .filelist
	@echo controller/jar.deps >> .filelist
	@echo satellite/jar.deps >> .filelist
	@echo server/libs >> .filelist
	@echo controller/libs >> .filelist
	@echo satellite/libs >> .filelist
	@echo .filelist >> .filelist
	@echo "./.filelist updated."


tgz:
	test -s .filelist
	@if [ ! -d .git ]; then \
		echo >&2 "Not a git directory!"; exit 1; \
	fi; \
	tar --transform="s,^,linstor-server-$(VERSION)/,S"         \
	   --owner=0 --group=0 -czf - -T .filelist > linstor-server-$(VERSION).tar.gz

# we cannot use 'git submodule foreach':
# foreach only works if submodule already checked out
.PHONY: check-submods
check-submods:
	@if test -d .git && test -s .gitmodules; then \
		for d in `grep "^\[submodule" .gitmodules | cut -f2 -d'"'`; do \
			if [ ! "`ls -A $$d`" ]; then \
				git submodule init; \
				git submodule update; \
				break; \
			fi; \
		done; \
	fi

prepare_release: tarball

release: prepare_release

debrelease:
	make tarball PRESERVE_DEBIAN=1 KEEPNAME=1

.PHONY: check-all-committed
check-all-committed:
	if ! tmp=$$(git diff --name-status HEAD 2>&1) || test -n "$$tmp" ; then \
		echo >&2 "$$tmp"; echo >&2 "Uncommitted changes"; git diff; exit 1; \
	fi
ifneq ($(FORCE),1)
	if ! grep -q "^linstor-server ($(VERSION)" debian/changelog ; then \
		echo >&2 "debian/changelog needs update"; exit 1; \
	fi
	for df in "$(DOCKERFILE_CONTROLLER)" "$(DOCKERFILE_SATELLITE)"; do \
		if ! grep -q "^ENV LINSTOR_VERSION $(VERSION)" "$$df" ; then \
			echo >&2 "$$df needs update"; exit 1; \
		fi; \
	done
endif

.PHONY: getprotc
getprotc:
	@gradle getprotoc

.PHONY: gen-java
gen-java: getprotc
	@gradle generateJava
	@echo "generated java sources"

.PHONY: copytolibs
copytolibs:
	gradle --console plain copytolibs

server/jar.deps: build.gradle
	gradle -q showServerRuntimeLibs > $@

controller/jar.deps satellite/jar.deps: build.gradle copytolibs
	./scripts/diffcopy.py -n ./controller/libs/runtime ./server/libs/runtime /usr/share/linstor-server/lib > controller/jar.deps
	sed -i '/^|usr|share|linstor-server|lib|server-/d' controller/jar.deps
	./scripts/diffcopy.py -n ./satellite/libs/runtime ./server/libs/runtime /usr/share/linstor-server/lib > satellite/jar.deps
	sed -i '/^|usr|share|linstor-server|lib|server-/d' satellite/jar.deps

tarball: check-all-committed check-submods versioninfo gen-java server/jar.deps controller/jar.deps satellite/jar.deps .filelist
	$(MAKE) tgz

versioninfo:
	mkdir $(GENRES) || true
	echo "version=$(VERSION)" > $(VERSINFO)
	echo "git.commit.id=$(GITHASH)" >> $(VERSINFO)
	echo "build.time=$$(date -u --iso-8601=second)" >> $(VERSINFO)

.PHONY: dockerimage dockerimage.controller dockerimage.satellite dockerpatch
dockerimage.controller:
	docker build -f $(DOCKERFILE_CONTROLLER) -t $(DOCKERREGPATH_CONTROLLER):$(DOCKER_TAG) .
	docker tag $(DOCKERREGPATH_CONTROLLER):$(DOCKER_TAG) $(DOCKERREGPATH_CONTROLLER):latest

dockerimage.satellite:
	docker build -f $(DOCKERFILE_SATELLITE) -t $(DOCKERREGPATH_SATELLITE):$(DOCKER_TAG) .
	docker tag $(DOCKERREGPATH_SATELLITE):$(DOCKER_TAG) $(DOCKERREGPATH_SATELLITE):latest

ifneq ($(FORCE),1)
dockerimage: debrelease dockerimage.controller dockerimage.satellite
else
dockerimage: dockerimage.controller dockerimage.satellite
endif

dockerpath:
	@echo $(DOCKERREGPATH_CONTROLLER):latest $(DOCKERREGPATH_CONTROLLER):$(DOCKER_TAG) $(DOCKERREGPATH_SATELLITE):latest $(DOCKERREGPATH_SATELLITE):$(DOCKER_TAG)

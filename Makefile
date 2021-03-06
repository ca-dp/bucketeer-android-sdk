PROTO_SRC_DIR := ../bucketeer/proto
PROTO_DIST_DIR := bucketeer/src/main/proto/proto
PROTO_HEADER := bucketeer/proto_header

.PHONY: copy-protos
copy-protos: .clean-protos .copy-event-protos .copy-feature-protos .copy-gateway-protos .copy-user-protos

.PHONY: .clean-protos
.clean-protos:
	rm -rf $(PROTO_DIST_DIR)

.PHONY: .copy-event-protos
.copy-event-protos:
	mkdir -p $(PROTO_DIST_DIR)/event/client
	cp $(PROTO_SRC_DIR)/event/client/event.proto $(PROTO_DIST_DIR)/event/client/event.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/event/client/event.proto

.PHONY: .copy-feature-protos
.copy-feature-protos:
	mkdir -p $(PROTO_DIST_DIR)/feature
	cp $(PROTO_SRC_DIR)/feature/evaluation.proto $(PROTO_DIST_DIR)/feature/evaluation.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/feature/evaluation.proto
	cp $(PROTO_SRC_DIR)/feature/reason.proto $(PROTO_DIST_DIR)/feature/reason.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/feature/reason.proto
	cp $(PROTO_SRC_DIR)/feature/variation.proto $(PROTO_DIST_DIR)/feature/variation.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/feature/variation.proto

.PHONY: .copy-gateway-protos
.copy-gateway-protos:
	mkdir -p $(PROTO_DIST_DIR)/gateway
	cp $(PROTO_SRC_DIR)/gateway/service.proto $(PROTO_DIST_DIR)/gateway/service.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/gateway/service.proto

.PHONY: .copy-user-protos
.copy-user-protos:
	mkdir -p $(PROTO_DIST_DIR)/user
	cp $(PROTO_SRC_DIR)/user/user.proto $(PROTO_DIST_DIR)/user/user.proto
	cat $(PROTO_HEADER) >> $(PROTO_DIST_DIR)/user/user.proto

package com.lingzerg.gamecenter.proto;

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ActionNoticePro.proto

public final class ActionNoticePro {
  private ActionNoticePro() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ActionNoticeProtoOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required int32 playerId = 1;
    /**
     * <code>required int32 playerId = 1;</code>
     */
    boolean hasPlayerId();
    /**
     * <code>required int32 playerId = 1;</code>
     */
    int getPlayerId();

    // required int32 seat = 2;
    /**
     * <code>required int32 seat = 2;</code>
     */
    boolean hasSeat();
    /**
     * <code>required int32 seat = 2;</code>
     */
    int getSeat();

    // required int32 remainBankroll = 3;
    /**
     * <code>required int32 remainBankroll = 3;</code>
     *
     * <pre>
     *剩余筹码
     * </pre>
     */
    boolean hasRemainBankroll();
    /**
     * <code>required int32 remainBankroll = 3;</code>
     *
     * <pre>
     *剩余筹码
     * </pre>
     */
    int getRemainBankroll();

    // required bool checkable = 4;
    /**
     * <code>required bool checkable = 4;</code>
     *
     * <pre>
     *能否让牌
     * </pre>
     */
    boolean hasCheckable();
    /**
     * <code>required bool checkable = 4;</code>
     *
     * <pre>
     *能否让牌
     * </pre>
     */
    boolean getCheckable();

    // required int32 minCall = 5;
    /**
     * <code>required int32 minCall = 5;</code>
     *
     * <pre>
     *下注下限
     * </pre>
     */
    boolean hasMinCall();
    /**
     * <code>required int32 minCall = 5;</code>
     *
     * <pre>
     *下注下限
     * </pre>
     */
    int getMinCall();
  }
  /**
   * Protobuf type {@code ActionNoticeProto}
   */
  public static final class ActionNoticeProto extends
      com.google.protobuf.GeneratedMessage
      implements ActionNoticeProtoOrBuilder {
    // Use ActionNoticeProto.newBuilder() to construct.
    private ActionNoticeProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private ActionNoticeProto(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final ActionNoticeProto defaultInstance;
    public static ActionNoticeProto getDefaultInstance() {
      return defaultInstance;
    }

    public ActionNoticeProto getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private ActionNoticeProto(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              playerId_ = input.readInt32();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              seat_ = input.readInt32();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              remainBankroll_ = input.readInt32();
              break;
            }
            case 32: {
              bitField0_ |= 0x00000008;
              checkable_ = input.readBool();
              break;
            }
            case 40: {
              bitField0_ |= 0x00000010;
              minCall_ = input.readInt32();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ActionNoticePro.internal_static_ActionNoticeProto_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ActionNoticePro.internal_static_ActionNoticeProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ActionNoticePro.ActionNoticeProto.class, ActionNoticePro.ActionNoticeProto.Builder.class);
    }

    public static com.google.protobuf.Parser<ActionNoticeProto> PARSER =
        new com.google.protobuf.AbstractParser<ActionNoticeProto>() {
      public ActionNoticeProto parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ActionNoticeProto(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<ActionNoticeProto> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required int32 playerId = 1;
    public static final int PLAYERID_FIELD_NUMBER = 1;
    private int playerId_;
    /**
     * <code>required int32 playerId = 1;</code>
     */
    public boolean hasPlayerId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int32 playerId = 1;</code>
     */
    public int getPlayerId() {
      return playerId_;
    }

    // required int32 seat = 2;
    public static final int SEAT_FIELD_NUMBER = 2;
    private int seat_;
    /**
     * <code>required int32 seat = 2;</code>
     */
    public boolean hasSeat() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required int32 seat = 2;</code>
     */
    public int getSeat() {
      return seat_;
    }

    // required int32 remainBankroll = 3;
    public static final int REMAINBANKROLL_FIELD_NUMBER = 3;
    private int remainBankroll_;
    /**
     * <code>required int32 remainBankroll = 3;</code>
     *
     * <pre>
     *剩余筹码
     * </pre>
     */
    public boolean hasRemainBankroll() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>required int32 remainBankroll = 3;</code>
     *
     * <pre>
     *剩余筹码
     * </pre>
     */
    public int getRemainBankroll() {
      return remainBankroll_;
    }

    // required bool checkable = 4;
    public static final int CHECKABLE_FIELD_NUMBER = 4;
    private boolean checkable_;
    /**
     * <code>required bool checkable = 4;</code>
     *
     * <pre>
     *能否让牌
     * </pre>
     */
    public boolean hasCheckable() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>required bool checkable = 4;</code>
     *
     * <pre>
     *能否让牌
     * </pre>
     */
    public boolean getCheckable() {
      return checkable_;
    }

    // required int32 minCall = 5;
    public static final int MINCALL_FIELD_NUMBER = 5;
    private int minCall_;
    /**
     * <code>required int32 minCall = 5;</code>
     *
     * <pre>
     *下注下限
     * </pre>
     */
    public boolean hasMinCall() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>required int32 minCall = 5;</code>
     *
     * <pre>
     *下注下限
     * </pre>
     */
    public int getMinCall() {
      return minCall_;
    }

    private void initFields() {
      playerId_ = 0;
      seat_ = 0;
      remainBankroll_ = 0;
      checkable_ = false;
      minCall_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasPlayerId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasSeat()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasRemainBankroll()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasCheckable()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasMinCall()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, playerId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, seat_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeInt32(3, remainBankroll_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeBool(4, checkable_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeInt32(5, minCall_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, playerId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, seat_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(3, remainBankroll_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(4, checkable_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(5, minCall_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static ActionNoticePro.ActionNoticeProto parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static ActionNoticePro.ActionNoticeProto parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static ActionNoticePro.ActionNoticeProto parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ActionNoticePro.ActionNoticeProto parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(ActionNoticePro.ActionNoticeProto prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code ActionNoticeProto}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements ActionNoticePro.ActionNoticeProtoOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ActionNoticePro.internal_static_ActionNoticeProto_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ActionNoticePro.internal_static_ActionNoticeProto_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ActionNoticePro.ActionNoticeProto.class, ActionNoticePro.ActionNoticeProto.Builder.class);
      }

      // Construct using ActionNoticePro.ActionNoticeProto.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        playerId_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        seat_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        remainBankroll_ = 0;
        bitField0_ = (bitField0_ & ~0x00000004);
        checkable_ = false;
        bitField0_ = (bitField0_ & ~0x00000008);
        minCall_ = 0;
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ActionNoticePro.internal_static_ActionNoticeProto_descriptor;
      }

      public ActionNoticePro.ActionNoticeProto getDefaultInstanceForType() {
        return ActionNoticePro.ActionNoticeProto.getDefaultInstance();
      }

      public ActionNoticePro.ActionNoticeProto build() {
        ActionNoticePro.ActionNoticeProto result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ActionNoticePro.ActionNoticeProto buildPartial() {
        ActionNoticePro.ActionNoticeProto result = new ActionNoticePro.ActionNoticeProto(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.playerId_ = playerId_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.seat_ = seat_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.remainBankroll_ = remainBankroll_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.checkable_ = checkable_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.minCall_ = minCall_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ActionNoticePro.ActionNoticeProto) {
          return mergeFrom((ActionNoticePro.ActionNoticeProto)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ActionNoticePro.ActionNoticeProto other) {
        if (other == ActionNoticePro.ActionNoticeProto.getDefaultInstance()) return this;
        if (other.hasPlayerId()) {
          setPlayerId(other.getPlayerId());
        }
        if (other.hasSeat()) {
          setSeat(other.getSeat());
        }
        if (other.hasRemainBankroll()) {
          setRemainBankroll(other.getRemainBankroll());
        }
        if (other.hasCheckable()) {
          setCheckable(other.getCheckable());
        }
        if (other.hasMinCall()) {
          setMinCall(other.getMinCall());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasPlayerId()) {
          
          return false;
        }
        if (!hasSeat()) {
          
          return false;
        }
        if (!hasRemainBankroll()) {
          
          return false;
        }
        if (!hasCheckable()) {
          
          return false;
        }
        if (!hasMinCall()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        ActionNoticePro.ActionNoticeProto parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ActionNoticePro.ActionNoticeProto) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required int32 playerId = 1;
      private int playerId_ ;
      /**
       * <code>required int32 playerId = 1;</code>
       */
      public boolean hasPlayerId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required int32 playerId = 1;</code>
       */
      public int getPlayerId() {
        return playerId_;
      }
      /**
       * <code>required int32 playerId = 1;</code>
       */
      public Builder setPlayerId(int value) {
        bitField0_ |= 0x00000001;
        playerId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 playerId = 1;</code>
       */
      public Builder clearPlayerId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        playerId_ = 0;
        onChanged();
        return this;
      }

      // required int32 seat = 2;
      private int seat_ ;
      /**
       * <code>required int32 seat = 2;</code>
       */
      public boolean hasSeat() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required int32 seat = 2;</code>
       */
      public int getSeat() {
        return seat_;
      }
      /**
       * <code>required int32 seat = 2;</code>
       */
      public Builder setSeat(int value) {
        bitField0_ |= 0x00000002;
        seat_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 seat = 2;</code>
       */
      public Builder clearSeat() {
        bitField0_ = (bitField0_ & ~0x00000002);
        seat_ = 0;
        onChanged();
        return this;
      }

      // required int32 remainBankroll = 3;
      private int remainBankroll_ ;
      /**
       * <code>required int32 remainBankroll = 3;</code>
       *
       * <pre>
       *剩余筹码
       * </pre>
       */
      public boolean hasRemainBankroll() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>required int32 remainBankroll = 3;</code>
       *
       * <pre>
       *剩余筹码
       * </pre>
       */
      public int getRemainBankroll() {
        return remainBankroll_;
      }
      /**
       * <code>required int32 remainBankroll = 3;</code>
       *
       * <pre>
       *剩余筹码
       * </pre>
       */
      public Builder setRemainBankroll(int value) {
        bitField0_ |= 0x00000004;
        remainBankroll_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 remainBankroll = 3;</code>
       *
       * <pre>
       *剩余筹码
       * </pre>
       */
      public Builder clearRemainBankroll() {
        bitField0_ = (bitField0_ & ~0x00000004);
        remainBankroll_ = 0;
        onChanged();
        return this;
      }

      // required bool checkable = 4;
      private boolean checkable_ ;
      /**
       * <code>required bool checkable = 4;</code>
       *
       * <pre>
       *能否让牌
       * </pre>
       */
      public boolean hasCheckable() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>required bool checkable = 4;</code>
       *
       * <pre>
       *能否让牌
       * </pre>
       */
      public boolean getCheckable() {
        return checkable_;
      }
      /**
       * <code>required bool checkable = 4;</code>
       *
       * <pre>
       *能否让牌
       * </pre>
       */
      public Builder setCheckable(boolean value) {
        bitField0_ |= 0x00000008;
        checkable_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required bool checkable = 4;</code>
       *
       * <pre>
       *能否让牌
       * </pre>
       */
      public Builder clearCheckable() {
        bitField0_ = (bitField0_ & ~0x00000008);
        checkable_ = false;
        onChanged();
        return this;
      }

      // required int32 minCall = 5;
      private int minCall_ ;
      /**
       * <code>required int32 minCall = 5;</code>
       *
       * <pre>
       *下注下限
       * </pre>
       */
      public boolean hasMinCall() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      /**
       * <code>required int32 minCall = 5;</code>
       *
       * <pre>
       *下注下限
       * </pre>
       */
      public int getMinCall() {
        return minCall_;
      }
      /**
       * <code>required int32 minCall = 5;</code>
       *
       * <pre>
       *下注下限
       * </pre>
       */
      public Builder setMinCall(int value) {
        bitField0_ |= 0x00000010;
        minCall_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 minCall = 5;</code>
       *
       * <pre>
       *下注下限
       * </pre>
       */
      public Builder clearMinCall() {
        bitField0_ = (bitField0_ & ~0x00000010);
        minCall_ = 0;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:ActionNoticeProto)
    }

    static {
      defaultInstance = new ActionNoticeProto(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:ActionNoticeProto)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_ActionNoticeProto_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_ActionNoticeProto_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025ActionNoticePro.proto\"o\n\021ActionNoticeP" +
      "roto\022\020\n\010playerId\030\001 \002(\005\022\014\n\004seat\030\002 \002(\005\022\026\n\016" +
      "remainBankroll\030\003 \002(\005\022\021\n\tcheckable\030\004 \002(\010\022" +
      "\017\n\007minCall\030\005 \002(\005"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_ActionNoticeProto_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_ActionNoticeProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_ActionNoticeProto_descriptor,
              new java.lang.String[] { "PlayerId", "Seat", "RemainBankroll", "Checkable", "MinCall", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}

package com.lingzerg.gamecenter.proto;

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: RaisePro.proto

public final class RaisePro {
  private RaisePro() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface RaiseProtoOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required int32 ante = 2;
    /**
     * <code>required int32 ante = 2;</code>
     *
     * <pre>
     *加注金额
     * </pre>
     */
    boolean hasAnte();
    /**
     * <code>required int32 ante = 2;</code>
     *
     * <pre>
     *加注金额
     * </pre>
     */
    int getAnte();
  }
  /**
   * Protobuf type {@code RaiseProto}
   */
  public static final class RaiseProto extends
      com.google.protobuf.GeneratedMessage
      implements RaiseProtoOrBuilder {
    // Use RaiseProto.newBuilder() to construct.
    private RaiseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private RaiseProto(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final RaiseProto defaultInstance;
    public static RaiseProto getDefaultInstance() {
      return defaultInstance;
    }

    public RaiseProto getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private RaiseProto(
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
            case 16: {
              bitField0_ |= 0x00000001;
              ante_ = input.readInt32();
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
      return RaisePro.internal_static_RaiseProto_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return RaisePro.internal_static_RaiseProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              RaisePro.RaiseProto.class, RaisePro.RaiseProto.Builder.class);
    }

    public static com.google.protobuf.Parser<RaiseProto> PARSER =
        new com.google.protobuf.AbstractParser<RaiseProto>() {
      public RaiseProto parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new RaiseProto(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<RaiseProto> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required int32 ante = 2;
    public static final int ANTE_FIELD_NUMBER = 2;
    private int ante_;
    /**
     * <code>required int32 ante = 2;</code>
     *
     * <pre>
     *加注金额
     * </pre>
     */
    public boolean hasAnte() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required int32 ante = 2;</code>
     *
     * <pre>
     *加注金额
     * </pre>
     */
    public int getAnte() {
      return ante_;
    }

    private void initFields() {
      ante_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasAnte()) {
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
        output.writeInt32(2, ante_);
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
          .computeInt32Size(2, ante_);
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

    public static RaisePro.RaiseProto parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static RaisePro.RaiseProto parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static RaisePro.RaiseProto parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static RaisePro.RaiseProto parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static RaisePro.RaiseProto parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static RaisePro.RaiseProto parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static RaisePro.RaiseProto parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static RaisePro.RaiseProto parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static RaisePro.RaiseProto parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static RaisePro.RaiseProto parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(RaisePro.RaiseProto prototype) {
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
     * Protobuf type {@code RaiseProto}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements RaisePro.RaiseProtoOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return RaisePro.internal_static_RaiseProto_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return RaisePro.internal_static_RaiseProto_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                RaisePro.RaiseProto.class, RaisePro.RaiseProto.Builder.class);
      }

      // Construct using RaisePro.RaiseProto.newBuilder()
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
        ante_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return RaisePro.internal_static_RaiseProto_descriptor;
      }

      public RaisePro.RaiseProto getDefaultInstanceForType() {
        return RaisePro.RaiseProto.getDefaultInstance();
      }

      public RaisePro.RaiseProto build() {
        RaisePro.RaiseProto result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public RaisePro.RaiseProto buildPartial() {
        RaisePro.RaiseProto result = new RaisePro.RaiseProto(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.ante_ = ante_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof RaisePro.RaiseProto) {
          return mergeFrom((RaisePro.RaiseProto)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(RaisePro.RaiseProto other) {
        if (other == RaisePro.RaiseProto.getDefaultInstance()) return this;
        if (other.hasAnte()) {
          setAnte(other.getAnte());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasAnte()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        RaisePro.RaiseProto parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (RaisePro.RaiseProto) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required int32 ante = 2;
      private int ante_ ;
      /**
       * <code>required int32 ante = 2;</code>
       *
       * <pre>
       *加注金额
       * </pre>
       */
      public boolean hasAnte() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required int32 ante = 2;</code>
       *
       * <pre>
       *加注金额
       * </pre>
       */
      public int getAnte() {
        return ante_;
      }
      /**
       * <code>required int32 ante = 2;</code>
       *
       * <pre>
       *加注金额
       * </pre>
       */
      public Builder setAnte(int value) {
        bitField0_ |= 0x00000001;
        ante_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required int32 ante = 2;</code>
       *
       * <pre>
       *加注金额
       * </pre>
       */
      public Builder clearAnte() {
        bitField0_ = (bitField0_ & ~0x00000001);
        ante_ = 0;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:RaiseProto)
    }

    static {
      defaultInstance = new RaiseProto(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:RaiseProto)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_RaiseProto_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_RaiseProto_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016RaisePro.proto\"\032\n\nRaiseProto\022\014\n\004ante\030\002" +
      " \002(\005"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_RaiseProto_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_RaiseProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_RaiseProto_descriptor,
              new java.lang.String[] { "Ante", });
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

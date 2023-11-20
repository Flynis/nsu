// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: snakes.proto

package ru.dyakun.snake.protocol;

/**
 * Protobuf type {@code protocol.GameAnnouncement}
 */
public final class GameAnnouncement extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:protocol.GameAnnouncement)
    GameAnnouncementOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GameAnnouncement.newBuilder() to construct.
  private GameAnnouncement(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GameAnnouncement() {
    canJoin_ = true;
    gameName_ = "";
  }

  @Override
  @SuppressWarnings({"unused"})
  protected Object newInstance(
      UnusedPrivateParameter unused) {
    return new GameAnnouncement();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return Snakes.internal_static_protocol_GameAnnouncement_descriptor;
  }

  @Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return Snakes.internal_static_protocol_GameAnnouncement_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            GameAnnouncement.class, Builder.class);
  }

  private int bitField0_;
  public static final int PLAYERS_FIELD_NUMBER = 1;
  private GamePlayers players_;
  /**
   * <pre>
   * Текущие игроки
   * </pre>
   *
   * <code>required .protocol.GamePlayers players = 1;</code>
   * @return Whether the players field is set.
   */
  @Override
  public boolean hasPlayers() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <pre>
   * Текущие игроки
   * </pre>
   *
   * <code>required .protocol.GamePlayers players = 1;</code>
   * @return The players.
   */
  @Override
  public GamePlayers getPlayers() {
    return players_ == null ? GamePlayers.getDefaultInstance() : players_;
  }
  /**
   * <pre>
   * Текущие игроки
   * </pre>
   *
   * <code>required .protocol.GamePlayers players = 1;</code>
   */
  @Override
  public GamePlayersOrBuilder getPlayersOrBuilder() {
    return players_ == null ? GamePlayers.getDefaultInstance() : players_;
  }

  public static final int CONFIG_FIELD_NUMBER = 2;
  private GameConfig config_;
  /**
   * <pre>
   * Параметры игры
   * </pre>
   *
   * <code>required .protocol.GameConfig config = 2;</code>
   * @return Whether the config field is set.
   */
  @Override
  public boolean hasConfig() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <pre>
   * Параметры игры
   * </pre>
   *
   * <code>required .protocol.GameConfig config = 2;</code>
   * @return The config.
   */
  @Override
  public GameConfig getConfig() {
    return config_ == null ? GameConfig.getDefaultInstance() : config_;
  }
  /**
   * <pre>
   * Параметры игры
   * </pre>
   *
   * <code>required .protocol.GameConfig config = 2;</code>
   */
  @Override
  public GameConfigOrBuilder getConfigOrBuilder() {
    return config_ == null ? GameConfig.getDefaultInstance() : config_;
  }

  public static final int CAN_JOIN_FIELD_NUMBER = 3;
  private boolean canJoin_ = true;
  /**
   * <pre>
   * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
   * </pre>
   *
   * <code>optional bool can_join = 3 [default = true];</code>
   * @return Whether the canJoin field is set.
   */
  @Override
  public boolean hasCanJoin() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <pre>
   * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
   * </pre>
   *
   * <code>optional bool can_join = 3 [default = true];</code>
   * @return The canJoin.
   */
  @Override
  public boolean getCanJoin() {
    return canJoin_;
  }

  public static final int GAME_NAME_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private volatile Object gameName_ = "";
  /**
   * <pre>
   * Глобально уникальное имя игры, например "my game"
   * </pre>
   *
   * <code>required string game_name = 4;</code>
   * @return Whether the gameName field is set.
   */
  @Override
  public boolean hasGameName() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <pre>
   * Глобально уникальное имя игры, например "my game"
   * </pre>
   *
   * <code>required string game_name = 4;</code>
   * @return The gameName.
   */
  @Override
  public String getGameName() {
    Object ref = gameName_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      if (bs.isValidUtf8()) {
        gameName_ = s;
      }
      return s;
    }
  }
  /**
   * <pre>
   * Глобально уникальное имя игры, например "my game"
   * </pre>
   *
   * <code>required string game_name = 4;</code>
   * @return The bytes for gameName.
   */
  @Override
  public com.google.protobuf.ByteString
      getGameNameBytes() {
    Object ref = gameName_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      gameName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    if (!hasPlayers()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!hasConfig()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!hasGameName()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!getPlayers().isInitialized()) {
      memoizedIsInitialized = 0;
      return false;
    }
    memoizedIsInitialized = 1;
    return true;
  }

  @Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeMessage(1, getPlayers());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      output.writeMessage(2, getConfig());
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      output.writeBool(3, canJoin_);
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, gameName_);
    }
    getUnknownFields().writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getPlayers());
    }
    if (((bitField0_ & 0x00000002) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getConfig());
    }
    if (((bitField0_ & 0x00000004) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(3, canJoin_);
    }
    if (((bitField0_ & 0x00000008) != 0)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, gameName_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof GameAnnouncement)) {
      return super.equals(obj);
    }
    GameAnnouncement other = (GameAnnouncement) obj;

    if (hasPlayers() != other.hasPlayers()) return false;
    if (hasPlayers()) {
      if (!getPlayers()
          .equals(other.getPlayers())) return false;
    }
    if (hasConfig() != other.hasConfig()) return false;
    if (hasConfig()) {
      if (!getConfig()
          .equals(other.getConfig())) return false;
    }
    if (hasCanJoin() != other.hasCanJoin()) return false;
    if (hasCanJoin()) {
      if (getCanJoin()
          != other.getCanJoin()) return false;
    }
    if (hasGameName() != other.hasGameName()) return false;
    if (hasGameName()) {
      if (!getGameName()
          .equals(other.getGameName())) return false;
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasPlayers()) {
      hash = (37 * hash) + PLAYERS_FIELD_NUMBER;
      hash = (53 * hash) + getPlayers().hashCode();
    }
    if (hasConfig()) {
      hash = (37 * hash) + CONFIG_FIELD_NUMBER;
      hash = (53 * hash) + getConfig().hashCode();
    }
    if (hasCanJoin()) {
      hash = (37 * hash) + CAN_JOIN_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getCanJoin());
    }
    if (hasGameName()) {
      hash = (37 * hash) + GAME_NAME_FIELD_NUMBER;
      hash = (53 * hash) + getGameName().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static GameAnnouncement parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static GameAnnouncement parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static GameAnnouncement parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static GameAnnouncement parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static GameAnnouncement parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static GameAnnouncement parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static GameAnnouncement parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static GameAnnouncement parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static GameAnnouncement parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static GameAnnouncement parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static GameAnnouncement parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static GameAnnouncement parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(GameAnnouncement prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code protocol.GameAnnouncement}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:protocol.GameAnnouncement)
      GameAnnouncementOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return Snakes.internal_static_protocol_GameAnnouncement_descriptor;
    }

    @Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return Snakes.internal_static_protocol_GameAnnouncement_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              GameAnnouncement.class, Builder.class);
    }

    // Construct using ru.dyakun.snake.protocol.GameAnnouncement.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getPlayersFieldBuilder();
        getConfigFieldBuilder();
      }
    }
    @Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      players_ = null;
      if (playersBuilder_ != null) {
        playersBuilder_.dispose();
        playersBuilder_ = null;
      }
      config_ = null;
      if (configBuilder_ != null) {
        configBuilder_.dispose();
        configBuilder_ = null;
      }
      canJoin_ = true;
      gameName_ = "";
      return this;
    }

    @Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return Snakes.internal_static_protocol_GameAnnouncement_descriptor;
    }

    @Override
    public GameAnnouncement getDefaultInstanceForType() {
      return GameAnnouncement.getDefaultInstance();
    }

    @Override
    public GameAnnouncement build() {
      GameAnnouncement result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public GameAnnouncement buildPartial() {
      GameAnnouncement result = new GameAnnouncement(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(GameAnnouncement result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.players_ = playersBuilder_ == null
            ? players_
            : playersBuilder_.build();
        to_bitField0_ |= 0x00000001;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.config_ = configBuilder_ == null
            ? config_
            : configBuilder_.build();
        to_bitField0_ |= 0x00000002;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.canJoin_ = canJoin_;
        to_bitField0_ |= 0x00000004;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.gameName_ = gameName_;
        to_bitField0_ |= 0x00000008;
      }
      result.bitField0_ |= to_bitField0_;
    }

    @Override
    public Builder clone() {
      return super.clone();
    }
    @Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.setField(field, value);
    }
    @Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.addRepeatedField(field, value);
    }
    @Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof GameAnnouncement) {
        return mergeFrom((GameAnnouncement)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(GameAnnouncement other) {
      if (other == GameAnnouncement.getDefaultInstance()) return this;
      if (other.hasPlayers()) {
        mergePlayers(other.getPlayers());
      }
      if (other.hasConfig()) {
        mergeConfig(other.getConfig());
      }
      if (other.hasCanJoin()) {
        setCanJoin(other.getCanJoin());
      }
      if (other.hasGameName()) {
        gameName_ = other.gameName_;
        bitField0_ |= 0x00000008;
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @Override
    public final boolean isInitialized() {
      if (!hasPlayers()) {
        return false;
      }
      if (!hasConfig()) {
        return false;
      }
      if (!hasGameName()) {
        return false;
      }
      if (!getPlayers().isInitialized()) {
        return false;
      }
      return true;
    }

    @Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              input.readMessage(
                  getPlayersFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getConfigFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              canJoin_ = input.readBool();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 34: {
              gameName_ = input.readBytes();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private GamePlayers players_;
    private com.google.protobuf.SingleFieldBuilderV3<
        GamePlayers, GamePlayers.Builder, GamePlayersOrBuilder> playersBuilder_;
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     * @return Whether the players field is set.
     */
    public boolean hasPlayers() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     * @return The players.
     */
    public GamePlayers getPlayers() {
      if (playersBuilder_ == null) {
        return players_ == null ? GamePlayers.getDefaultInstance() : players_;
      } else {
        return playersBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public Builder setPlayers(GamePlayers value) {
      if (playersBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        players_ = value;
      } else {
        playersBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public Builder setPlayers(
        GamePlayers.Builder builderForValue) {
      if (playersBuilder_ == null) {
        players_ = builderForValue.build();
      } else {
        playersBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public Builder mergePlayers(GamePlayers value) {
      if (playersBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          players_ != null &&
          players_ != GamePlayers.getDefaultInstance()) {
          getPlayersBuilder().mergeFrom(value);
        } else {
          players_ = value;
        }
      } else {
        playersBuilder_.mergeFrom(value);
      }
      if (players_ != null) {
        bitField0_ |= 0x00000001;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public Builder clearPlayers() {
      bitField0_ = (bitField0_ & ~0x00000001);
      players_ = null;
      if (playersBuilder_ != null) {
        playersBuilder_.dispose();
        playersBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public GamePlayers.Builder getPlayersBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getPlayersFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    public GamePlayersOrBuilder getPlayersOrBuilder() {
      if (playersBuilder_ != null) {
        return playersBuilder_.getMessageOrBuilder();
      } else {
        return players_ == null ?
            GamePlayers.getDefaultInstance() : players_;
      }
    }
    /**
     * <pre>
     * Текущие игроки
     * </pre>
     *
     * <code>required .protocol.GamePlayers players = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        GamePlayers, GamePlayers.Builder, GamePlayersOrBuilder>
        getPlayersFieldBuilder() {
      if (playersBuilder_ == null) {
        playersBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            GamePlayers, GamePlayers.Builder, GamePlayersOrBuilder>(
                getPlayers(),
                getParentForChildren(),
                isClean());
        players_ = null;
      }
      return playersBuilder_;
    }

    private GameConfig config_;
    private com.google.protobuf.SingleFieldBuilderV3<
        GameConfig, GameConfig.Builder, GameConfigOrBuilder> configBuilder_;
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     * @return Whether the config field is set.
     */
    public boolean hasConfig() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     * @return The config.
     */
    public GameConfig getConfig() {
      if (configBuilder_ == null) {
        return config_ == null ? GameConfig.getDefaultInstance() : config_;
      } else {
        return configBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public Builder setConfig(GameConfig value) {
      if (configBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        config_ = value;
      } else {
        configBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public Builder setConfig(
        GameConfig.Builder builderForValue) {
      if (configBuilder_ == null) {
        config_ = builderForValue.build();
      } else {
        configBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public Builder mergeConfig(GameConfig value) {
      if (configBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          config_ != null &&
          config_ != GameConfig.getDefaultInstance()) {
          getConfigBuilder().mergeFrom(value);
        } else {
          config_ = value;
        }
      } else {
        configBuilder_.mergeFrom(value);
      }
      if (config_ != null) {
        bitField0_ |= 0x00000002;
        onChanged();
      }
      return this;
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public Builder clearConfig() {
      bitField0_ = (bitField0_ & ~0x00000002);
      config_ = null;
      if (configBuilder_ != null) {
        configBuilder_.dispose();
        configBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public GameConfig.Builder getConfigBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getConfigFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    public GameConfigOrBuilder getConfigOrBuilder() {
      if (configBuilder_ != null) {
        return configBuilder_.getMessageOrBuilder();
      } else {
        return config_ == null ?
            GameConfig.getDefaultInstance() : config_;
      }
    }
    /**
     * <pre>
     * Параметры игры
     * </pre>
     *
     * <code>required .protocol.GameConfig config = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        GameConfig, GameConfig.Builder, GameConfigOrBuilder>
        getConfigFieldBuilder() {
      if (configBuilder_ == null) {
        configBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            GameConfig, GameConfig.Builder, GameConfigOrBuilder>(
                getConfig(),
                getParentForChildren(),
                isClean());
        config_ = null;
      }
      return configBuilder_;
    }

    private boolean canJoin_ = true;
    /**
     * <pre>
     * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
     * </pre>
     *
     * <code>optional bool can_join = 3 [default = true];</code>
     * @return Whether the canJoin field is set.
     */
    @Override
    public boolean hasCanJoin() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <pre>
     * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
     * </pre>
     *
     * <code>optional bool can_join = 3 [default = true];</code>
     * @return The canJoin.
     */
    @Override
    public boolean getCanJoin() {
      return canJoin_;
    }
    /**
     * <pre>
     * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
     * </pre>
     *
     * <code>optional bool can_join = 3 [default = true];</code>
     * @param value The canJoin to set.
     * @return This builder for chaining.
     */
    public Builder setCanJoin(boolean value) {

      canJoin_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Можно ли новому игроку присоединиться к игре (есть ли место на поле)
     * </pre>
     *
     * <code>optional bool can_join = 3 [default = true];</code>
     * @return This builder for chaining.
     */
    public Builder clearCanJoin() {
      bitField0_ = (bitField0_ & ~0x00000004);
      canJoin_ = true;
      onChanged();
      return this;
    }

    private Object gameName_ = "";
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @return Whether the gameName field is set.
     */
    public boolean hasGameName() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @return The gameName.
     */
    public String getGameName() {
      Object ref = gameName_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          gameName_ = s;
        }
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @return The bytes for gameName.
     */
    public com.google.protobuf.ByteString
        getGameNameBytes() {
      Object ref = gameName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        gameName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @param value The gameName to set.
     * @return This builder for chaining.
     */
    public Builder setGameName(
        String value) {
      if (value == null) { throw new NullPointerException(); }
      gameName_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearGameName() {
      gameName_ = getDefaultInstance().getGameName();
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Глобально уникальное имя игры, например "my game"
     * </pre>
     *
     * <code>required string game_name = 4;</code>
     * @param value The bytes for gameName to set.
     * @return This builder for chaining.
     */
    public Builder setGameNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      gameName_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    @Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:protocol.GameAnnouncement)
  }

  // @@protoc_insertion_point(class_scope:protocol.GameAnnouncement)
  private static final GameAnnouncement DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new GameAnnouncement();
  }

  public static GameAnnouncement getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  @Deprecated public static final com.google.protobuf.Parser<GameAnnouncement>
      PARSER = new com.google.protobuf.AbstractParser<GameAnnouncement>() {
    @Override
    public GameAnnouncement parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<GameAnnouncement> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<GameAnnouncement> getParserForType() {
    return PARSER;
  }

  @Override
  public GameAnnouncement getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}


package net.lordofthecraft.aegis.packet;

import de.exceptionflug.protocolize.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MapData extends AbstractPacket {

  int mapID; //  Map ID of the map being modified
  byte scale; //  From 0 for a fully zoomed-in map (1 block per pixel) to 4 for a fully zoomed-out map (16 blocks per pixel)
  boolean trackingPosition; //  Specifies whether player and item frame icons are shown
  boolean locked; // True if the map has been locked in a cartography table
  Icon[] icons;

  int columns; // Number of columns updated
  int rows; // Only if Columns is more than 0; number of rows updated
  int xOffset; //  Only if Columns is more than 0; x offset of the westernmost column
  int zOffset; //  Only if Columns is more than 0; z offset of the northernmost row
  byte[] mapData;

  public class Icon {
    int type;
    byte x; // Map coordinates: -128 for furthest left, +127 for furthest right
    byte z; // Map coordinates: -128 for highest, +127 for lowest
    byte direction; // 0-15
    boolean hasDisplayName;
    String displayName;
  }

  void validate() {
    if(mapID < 0) error("map id");
    if(scale < 0 || scale > 4) error("scale");

    if(columns == 0) {
      if(rows > 0) error("rows");
      if(xOffset > 0) error("xOffset");
      if(zOffset > 0) error("zOffset");
      if(mapData.length > 0) error("mapData");
    } else {
      if(rows == 0) error("rows");
      if(mapData.length != rows * columns) error("mapData");
    }
  }

  private void error(String param) {
    throw new IllegalStateException("This packet has invalid parameters: " + param);
  }


  @Override
  public void read(ByteBuf buf){
    mapID = readVarInt(buf);
    scale = buf.readByte();
    trackingPosition = buf.readBoolean();
    locked = buf.readBoolean();

    int howManyIcons = readVarInt(buf);
    this.icons = new Icon[howManyIcons];
    for(int i = 0; i < howManyIcons; i++) {
      Icon icon = new Icon();

      icon.type = readVarInt(buf);
      icon.x = buf.readByte();
      icon.z = buf.readByte();
      icon.direction = buf.readByte();
      icon.hasDisplayName = buf.readBoolean();
      if(icon.hasDisplayName) {
        String json = readString(buf);
        BaseComponent[] cs = ComponentSerializer.parse(json);
        icon.displayName = new TextComponent(cs).toLegacyText();
      }

      icons[i] = icon;
    }

    columns = buf.readUnsignedByte();
    if(columns > 0) {
      rows = buf.readUnsignedByte();
      xOffset = buf.readUnsignedByte();
      zOffset = buf.readUnsignedByte();
      int dataLength = readVarInt(buf);
      if(dataLength > 0 ) {
        mapData = new byte[dataLength];
        buf.readBytes(mapData);
      }
    }
  }


  @Override
  public void write(ByteBuf buf) {
    validate();

    writeVarInt(mapID, buf);
    buf.writeByte(scale);
    buf.writeBoolean(trackingPosition);
    buf.writeBoolean(locked);
    writeVarInt(icons.length, buf);

    for(Icon icon : icons) {
      writeVarInt(icon.type, buf);
      buf.writeByte(icon.x);
      buf.writeByte(icon.z);
      buf.writeByte(icon.direction);
      buf.writeBoolean(icon.hasDisplayName);
      if(icon.hasDisplayName) {
        writeString(ComponentSerializer.toString(TextComponent.fromLegacyText(icon.displayName)), buf);
      }
    }

    buf.writeByte(this.columns);
    if(this.columns > 0) {
      buf.writeByte(this.rows);
      buf.writeByte(this.xOffset);
      buf.writeByte(this.zOffset);
      writeVarInt(mapData.length, buf);
      buf.writeBytes(mapData);
    }
  }
}

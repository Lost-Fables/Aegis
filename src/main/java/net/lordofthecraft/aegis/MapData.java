package net.lordofthecraft.aegis;

import de.exceptionflug.protocolize.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MapData extends AbstractPacket {

    int mapID;
    byte scale;
    boolean trackingPosition;
    Icon[] icons;
    
    byte columns;
    byte rows;
    byte xOffset;
    byte zOffset;
    byte[] mapData;

    class Icon {
        int type;
        byte x;
        byte z;
        byte direction;
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
    	
    }
    
    

    @Override
    public void write(ByteBuf buf) {
    	writeVarInt(mapID, buf);
    	buf.writeByte(scale);
    	buf.writeBoolean(trackingPosition);
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

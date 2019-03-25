package net.lordofthecraft.aegis;

import de.exceptionflug.protocolize.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MapData extends AbstractPacket {

    int mapID;
    byte scale;
    boolean trackingPosition;
    int iconCount;
    Icon icon;
    byte columns;


    class Icon {
        int type;
        byte x;
        byte z;
        byte direction;
        boolean hasDisplayName;
        String displayName;
    }

    @Override
    public void read(ByteBuf buf){

    }

    @Override
    public void write(ByteBuf buf) {

    }
}

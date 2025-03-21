package transfer_code_test.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.velocity.util.ArrayListWrapper;
import org.armada.galileo.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobo
 * @date 2022/1/24 7:54 下午
 */
public class AdiServerDecoder extends ByteToMessageDecoder {

    private byte[] signHead;

    private byte[] signEnd;

    private boolean readHead = true;

    private boolean readEnd = false;

    public AdiServerDecoder(byte[] signHead, byte[] signEnd) {
        this.signHead = signHead;
        this.signEnd = signEnd;
    }

    int startIndex = -1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {
        if (readHead) {
            startIndex = indexOf(buffer, signHead);
            // System.out.println("startIndex: " + startIndex);
            if (startIndex >= 0) {
                readEnd = true;
                readHead = false;
                buffer.skipBytes(signHead.length);
            }
        }

        if (readEnd) {
            int endIndex = indexOf(buffer, signEnd);
            // System.out.println("endIndex: " + endIndex);
            if (endIndex >= 0) {
                readEnd = false;
                readHead = true;

                ByteBuf frame = buffer.readRetainedSlice(endIndex - (startIndex + signHead.length));
                buffer.skipBytes(signEnd.length);

                list.add(frame);
            }
        }
    }

    private static int indexOf(ByteBuf buffer, byte[] needle) {

//        List<Byte> arr = new ArrayList();
//        for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
//            arr.add(buffer.getByte(i));
//        }
//        byte[] bufs = new byte[arr.size()];
//        for (int i = 0; i < bufs.length; i++) {
//            bufs[i] = arr.get(i);
//        }
//        System.out.println(new String(bufs));
//        System.out.println(new String(needle));

        for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {

            for (int c = 0; c < needle.length; c++) {

                int parentIndex = c + i;

                if (parentIndex > buffer.writerIndex()) {
                    return -1;
                }

                if (buffer.getByte(parentIndex) != needle[c]) {
                    break;
                }

                // 对比到最后一个了
                if (c == needle.length - 1) {
                    return i;
                }
            }
        }
        return -1;
    }
}

package org.yzh.protocol.uwb;

/**
 * UWB协议CRC16校验算法
 * 使用多项式0xA123
 * 
 * @author yzh
 */
public class UWBCRC16 {

    private static final int POLY = 0xA123;

    /**
     * 计算CRC16校验码
     * 
     * @param data 待校验的数据
     * @return CRC16校验码
     */
    public int calculate(byte[] data) {
        return calculate(data, 0, data.length, 0);
    }

    /**
     * 计算CRC16校验码
     * 
     * @param data 待校验的数据
     * @param offset 起始偏移量
     * @param length 数据长度
     * @param initialCRC 初始CRC值
     * @return CRC16校验码
     */
    public int calculate(byte[] data, int offset, int length, int initialCRC) {
        int crc = initialCRC;
        
        for (int i = offset; i < offset + length; i++) {
            crc = crc ^ ((data[i] & 0xFF) << 8);
            
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLY;
                } else {
                    crc = crc << 1;
                }
            }
            
            crc &= 0xFFFF;
        }
        
        return crc;
    }

    /**
     * 验证CRC校验码
     * 
     * @param data 数据（包含CRC）
     * @param crcOffset CRC在数据中的偏移量
     * @return 校验是否通过
     */
    public boolean verify(byte[] data, int crcOffset) {
        if (data.length < crcOffset + 2) {
            return false;
        }
        
        // 计算数据部分的CRC
        int calculatedCRC = calculate(data, 0, crcOffset, 0);
        
        // 读取数据中的CRC（小端序）
        int dataCRC = ((data[crcOffset + 1] & 0xFF) << 8) | (data[crcOffset] & 0xFF);
        
        return calculatedCRC == dataCRC;
    }
}

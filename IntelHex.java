import java.util.List;

public class IntelHex {
	static byte h2b(String hex, int pos) {
		try {
			return (byte)Integer.parseInt(hex.substring(pos,pos+2), 16);
		} catch (NumberFormatException e) {
			return 0x00;
		}
	}

	static String toHex(SettingType s) {
		StringBuilder string = new StringBuilder(s.size()/4);
		int mask = 0xFF;
		for (int i=0; i<s.size()/8; i++) {
			string.append(String.format("%02X", (((Integer)s.get())&mask)>>(8*i)));
			mask <<= 8;
		}
		String str = string.toString();
		return str;
	}

	static String toBytes(SettingType s, String data) {
		int value=0;
		int bytes = s.size()/8;
		String item = data.substring(0,2*bytes);
		for (int i=bytes-1; i>=0; i--) {
			value <<= 8;
			value |= h2b(item,2*i)&0xFF;
		}
		value &= (1<<s.size())-1;
		s.set(value);
		return data.substring(2*bytes);
	}

	static String buildEeprom(String data) {
		int ROW_SIZE = 16;
		int size = data.length()/2;
		int start = 0;
		short address = 0;
		StringBuilder string = new StringBuilder();
		for (int i=0; i<(size+ROW_SIZE-1)/ROW_SIZE; i++) {
			int bytes = size-i*ROW_SIZE;
			if (bytes > ROW_SIZE) bytes = ROW_SIZE;
			Record row = new Record(address, data.substring(start,start+2*bytes));
			string.append(row);
			address += bytes;
			start += 2*bytes;
		}
		string.append(Record.EOF);
		return string.toString();
	}

	static String decodeEeprom(String ihex) throws InvalidRecordException {
		String lines[] = ihex.split("\n"); //System.lineSeparator());

		int currentLine = 0;
		StringBuilder data = new StringBuilder();
		for (String line : lines) {
			try {
				currentLine++;
				Record row = new Record(line);
				if (row.type == Record.RecordType.EOF.ordinal()) {
					break;
				}
				data.append(row.data);
			} catch (InvalidRecordException e) {
				throw new InvalidRecordException("Corrupt EEPROM. Error on line "+currentLine+":\n\n"+e.getMessage());
			}
		}
		return data.toString();
	}

	static String settingsToString(List<SettingType> fields) {
		StringBuilder data = new StringBuilder();
		for (SettingType field : fields) {
			data.append(toHex(field));
		}
		return buildEeprom(data.toString());
	}

	static String stringToSettings(String data, List<SettingType> fields) throws InvalidRecordException {
		for (SettingType field : fields) {
			data = toBytes(field, data);
		}
		return data;
	}
}

class Record {
	byte count;
	short address;
	byte type;
	byte checksum;
	String data;

	public enum RecordType {
		DATA,
		EOF,
		EXTENDED_SEGMENT_ADDRESS,
		START_SEGMENT_ADDRESS,
		EXTENDED_LINEAR_ADDRESS,
		START_LINEAR_ADDRESS
	}

	public static final Record EOF = new Record((byte)0x00,(short)0x0000,RecordType.EOF,(byte)0xFF,"");

	public Record(byte count, short address, RecordType type, byte checksum, String data) {
		this.count = count;
		this.address = address;
		this.type = (byte)type.ordinal();
		this.checksum = checksum;
		this.data = data;
	}

	public Record(short address, RecordType type, String data) {
		this.count = (byte)(data.length()/2);
		this.address = address;
		this.type = (byte)type.ordinal();
		this.data = data;
		this.checksum = checksum();
	}

	public Record(short address, String data) {
		this(address, RecordType.DATA, data);
	}

	public Record(String row) throws InvalidRecordException {
		if (row.charAt(0)!=':') {
			throw new InvalidRecordException("Not Intel Hex Format. Expected ':' at start of record.");
		}
		row = row.substring(1);
		this.count = IntelHex.h2b(row,0);
		int calculatedLength = 2+4+2+2*count+2;
		if (row.length() != calculatedLength) {
			throw new InvalidRecordException(String.format("Malformed Record. Expected '%d' bytes, found '%d'.", calculatedLength/2, row.length()/2));
		}
		this.address = IntelHex.h2b(row,2);
		this.address |= IntelHex.h2b(row,4) << 8;
		this.type = IntelHex.h2b(row,6);
		this.data = row.substring(8,8+2*count);
		this.checksum = IntelHex.h2b(row, 8+2*count);
		byte calculatedChecksum = checksum();
		if (calculatedChecksum != this.checksum) {
			throw new InvalidRecordException(String.format("Invalid Checksum. Expected '%02X', found '%02X'.", calculatedChecksum, this.checksum));
		}
	}

	public String toString() {
		return String.format(":%02X%04X%02X%s%02X%s", count, address, type, data, checksum, "\n"); //System.lineSeparator());
	}

	private byte checksum() {
		byte sum = 0;
		sum += count;
		sum += address & 0xFF;
		sum += (address & 0xFF00) >> 8;
		sum += type;
		for (int i=0; i<data.length(); i+=2) {
			sum += IntelHex.h2b(data,i);
		}
		return (byte)(~sum+1);
	}
}

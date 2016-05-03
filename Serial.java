import java.util.*;
import java.io.*;
import gnu.io.*;

public class Serial {
	NRSerialPort serial;
	DataInputStream in;
	DataOutputStream out;

	public void cmd(String cmd) throws IOException {
		send(cmd+"\n");
		read();
	}

	public void send(String data) throws IOException {
		out.writeBytes(data);
		out.flush();
	}

	public ArrayList<String> read() throws IOException {
		String s;
		ArrayList<String> lines = new ArrayList<String>();
		while ((s = in.readLine()) != null) {
			lines.add(s);
			System.out.println(s);
		}
		return lines;
	}


	public Set<String> listDevices() {
		return NRSerialPort.getAvailableSerialPorts();
	}

	public boolean upload(String device, String data) {
		int baudRate = 9600; //115200;


		serial = new NRSerialPort(device, baudRate);

		try {
			serial.connect();
			in = new DataInputStream(serial.getInputStream());
			out = new DataOutputStream(serial.getOutputStream());

			cmd("load");

			int i=0;
			String rows[] = data.split("\n");
			for (String row : rows) {
				i++;
				System.out.println(String.format("Uploading... row %d/%d (%.0f%%)", i, rows.length, 100.0*i/rows.length));

				send(row);
				try {
					Thread.sleep(100);
				} catch (Exception e) {}
				ArrayList<String> ret = read();
				if (ret.size()==0 || !"OK".equals(ret.get(0))) {
					return false;
				}
			}

			cmd("read");
			return true;
		} /*catch (NoSuchPortException e) {
			System.out.println("Could not connect to port: "+port);
		}*/ catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serial.isConnected()) {
				serial.disconnect();
			}
		}
		return false;
	}
}

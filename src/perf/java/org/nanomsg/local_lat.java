package org.nanomsg;

import java.nio.ByteBuffer;

public class local_lat {
	public static void main(String[] args) {
//        if (args.length != 3) {
//            System.out.printf("argc was %d\n", args.length);
//            System.out.printf("usage: local_lat <bind-to> <message-size> <roundtrip-count>\n");
//            return;
//        }

		NanoLibrary nano = new NanoLibrary();

		String bind_to;
		int roundtrip_count;
		int message_size;
		int socket = 0;
		int rc;
		int i;

		bind_to = "tcp://127.0.0.1:8888";// args[0];
		message_size = 1024;// Integer.parseInt(args[1]);
		roundtrip_count = (int) 1e6;// Integer.parseInt(args[2]);
		System.out.printf("args: %s | %d | %d\n", bind_to, message_size, roundtrip_count);

		socket = nano.nn_socket(nano.AF_SP, nano.NN_PAIR);
		if (socket < 0) {
			System.out.printf("error in nn_socket: %s\n", nano.nn_strerror(nano.nn_errno()));
			return;
		}
		System.out.printf("NANO PAIR socket created\n");

		int opt = 1;
		rc = nano.nn_setsockopt_int(socket, nano.NN_TCP, nano.NN_TCP_NODELAY, opt);
		if (rc < 0) {
			System.out.printf("error in nn_setsockopt(%d, %d, %d, %d): %s\n", socket, nano.NN_TCP, nano.NN_TCP_NODELAY,
					opt, nano.nn_strerror(nano.nn_errno()));
			return;
		}
		System.out.printf("NANO PAIR socket TCP option NODELAY set to %d\n", opt);

		rc = nano.nn_bind(socket, bind_to);
		if (rc < 0) {
			System.out.printf("error in nn_bind(%s): %s\n", bind_to, nano.nn_strerror(nano.nn_errno()));
			return;
		}
		System.out.printf("NANO PAIR socket bound to %s\n", bind_to);

		ByteBuffer bb = ByteBuffer.allocateDirect(message_size);
		byte bval = 111;
		for (i = 0; i < message_size; ++i) {
			bb.put(i, bval);
		}

		System.out.printf("NANO running %d iterations...\n", roundtrip_count);
		for (i = 0; i != roundtrip_count; i++) {
			bb.clear();
			rc = nano.nn_recv(socket, bb, 0);
			if (rc < 0) {
				System.out.printf("error in nn_recv: %s\n", nano.nn_strerror(nano.nn_errno()));
				return;
			}
			if (rc != message_size) {
				System.out.printf("message of incorrect size received\n");
				return;
			}

			bb.flip();
			rc = nano.nn_send(socket, bb, 0);
			if (rc < 0) {
				System.out.printf("error in nn_send: %s\n", nano.nn_strerror(nano.nn_errno()));
				return;
			}
			if (rc != message_size) {
				System.out.printf("message of incorrect size sent\n");
				return;
			}
		}

		rc = nano.nn_close(socket);
		if (rc < 0) {
			System.out.printf("error in nn_close: %s\n", nano.nn_strerror(nano.nn_errno()));
			return;
		}

		System.out.printf("NANO done running\n");
	}
}

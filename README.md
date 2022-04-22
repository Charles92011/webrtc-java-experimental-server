This project uses https://github.com/devopvoid/webrtc-java to create a java WebRTC server. The server accepts connections and will either mirror back the tracks, or connect to connections together.

It is merely an experiment to to learn to work with webrtc-java and collaborate with other developers using the library. 

This project is designed from the standpoint of a server, it handles connections and cross-connects peers. It's not in my interest to perform any media handling in a desktop application.

More complex features, and robust error handling are specifically left out of the project to make the basic mechanical processes easy to understand.

To run: Run Main.java, and load wsclient.html into a browser. 
On wsclient.html click start.

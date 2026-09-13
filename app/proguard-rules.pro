# fastexcel menulis XML lewat StAX; simpan kelasnya utuh.
-keep class org.dhatim.fastexcel.** { *; }
-dontwarn org.dhatim.fastexcel.**

# StAX (javax.xml.stream) dipakai fastexcel; ada di desugar/JDK, jangan ganggu.
-dontwarn javax.xml.stream.**
-dontwarn com.fasterxml.**

package com.pingan.plugin.utils

import org.apache.commons.io.FileUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Create by ChenWei on 2018/8/30 14:57
 **/
class JarZipUtil {
    /**
     * 将该jar包解压到指定目录
     * @param jarPath jar包的绝对路径
     * @param destDirPath jar包解压后的保存路径
     * @return 返回该jar包中包含的所有class的完整类名类名集合，其中一条数据如：com.aitski.hotpatch.Xxxx.class
     */
    static List unzipJar(String jarPath, String destDirPath) {

        List list = new ArrayList()
        if (jarPath.endsWith('.jar')) {

            JarFile jarFile = new JarFile(jarPath)
            Enumeration<JarEntry> jarEntrys = jarFile.entries()
            while (jarEntrys.hasMoreElements()) {
                JarEntry jarEntry = jarEntrys.nextElement()
                if (jarEntry.directory) {
                    continue
                }
                String entryName = jarEntry.getName()
                if (entryName.endsWith('.class')) {
                    String className = entryName.replace('\\', '.').replace('/', '.')
                    list.add(className)
                }
                String outFileName = destDirPath + "/" + entryName
                File outFile = new File(outFileName)
                outFile.getParentFile().mkdirs()
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                FileOutputStream fileOutputStream = new FileOutputStream(outFile)
                fileOutputStream << inputStream
                fileOutputStream.close()
                inputStream.close()
            }
            jarFile.close()
        }
        return list
    }

/**
 * 重新打包jar
 * @param packagePath 将这个目录下的所有文件打包成jar
 * @param destPath 打包好的jar包的绝对路径
 */
    static void zipJar(String packagePath, String destPath) {
        println("*********" + packagePath)
        println("*********" + destPath)

//        File destFile = new File(destPath)
//        if (destFile.exists()) {
//            if (destFile.isDirectory()) {
//                FileUtils.deleteDirectory(destFile)
//            } else {
//                destFile.delete()
//            }
//        }
        println("begin********")

        File file = new File(packagePath)
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(destPath))
        file.eachFileRecurse { File f ->
//            println("********" + f.toString())
            String entryName = f.getAbsolutePath().substring(packagePath.length() + 1)
//            println("********" + entryName)
//            outputStream.putNextEntry(new JarEntry(entryName))
            if (!f.directory) {
//                JarEntry entry = new JarEntry("com/nmnet/bussiness1/Login" + System.currentTimeMillis() + ".class")
                JarEntry entry = new JarEntry(entryName)
                outputStream.putNextEntry(entry)
//                outputStream.write("我日你".getBytes("utf-8"))
//                outputStream.write("test".getBytes("utf-8"))
                InputStream inputStream = new FileInputStream(f)
                outputStream << inputStream
                inputStream.close()
//                outputStream.write("test".getBytes("utf-8"))
//                outputStream.flush()
            }
        }
        outputStream.close()
        println("end********")
    }

}

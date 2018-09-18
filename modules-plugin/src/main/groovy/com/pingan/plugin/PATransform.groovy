package com.pingan.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.pingan.plugin.utils.TransformUtil
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 *
 * Create by ChenWei on 2018/8/29 16:25
 *
 **/
class PATransform extends Transform {
    private Project mProject

    PATransform(Project project) {
        this.mProject = project
    }

    @Override
    String getName() {
        return "PATransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
//        return Sets.immutableEnumSet(QualifiedContent.Scope.SUB_PROJECTS, QualifiedContent.Scope.PROJECT)
    }

    @Override
    boolean isIncremental() {
        return false
    }


    @Override
    void transform(@NonNull TransformInvocation transformInvocation) {
        //首先获取构建类型并给mPool添加classPath
        transformInvocation.inputs.each { TransformInput input ->
            if (!input.directoryInputs.isEmpty()) {
                def directoryInputPath = input.directoryInputs[0].file.absolutePath
                TransformUtil.appendClassPath(mProject, directoryInputPath)
            }
        }

        transformInvocation.inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
//                println("======directoryInput***" + directoryInput.toString())
//                println("======subProject***" + TransformUtil.getNeedTransformProject(mProject))
                TransformUtil.handleDirInput(directoryInput.file.absolutePath, mProject)
//                println("&&&&&&&&&&directoryInput handle finish")
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
//                println("&&&&&&&&&&directoryInput copy finish")
            }
            //遍历jar文件，对jar不进行操作，但是要输出到指定目录
            input.jarInputs.each { JarInput jarInput ->
                println("======jarInput***" + jarInput.toString())
                def jarPath = jarInput.file.absolutePath

//                if (jarPath.endsWith("classes.jar") && jarPath.contains("build/intermediates/intermediate-jars") &&
//                        (jarPath.contains("bussiness1")
//                                || jarPath.contains("aladdin-interface-library")
//                                || jarPath.contains("aladdinModularization")
//                                || jarPath.contains("aladdinCore")
//                        )) {
//                    TransformUtil.handleJarInput(jarPath, mProject)
//                }
                if (jarPath.endsWith("classes.jar")) {
                    TransformUtil.handleJarInput(jarPath, mProject)
                }

//                println("&&&&&&&&&&jarInputs handle finish")
//                 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
//                println("&&&&&&&&&&jarInputs handle finish")
            }

        }

    }
}

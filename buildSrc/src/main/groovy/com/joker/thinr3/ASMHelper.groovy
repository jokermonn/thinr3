package com.joker.thinr3

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

import java.util.jar.JarEntry
import java.util.jar.JarFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ASMHelper {
  private static Map<String, Integer> map = [:]

  static void collectRInfo(File jar) {
    JarFile jarFile = new JarFile(jar)
    jarFile
        .entries()
        .grep { JarEntry entry -> isRFile(entry.name) }
        .each { JarEntry jarEntry ->
      jarFile.getInputStream(jarEntry).withStream { InputStream inputStream ->
        ClassReader classReader = new ClassReader(inputStream)
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM4) {
          @Override
          FieldVisitor visitField(int access, String name, String desc, String signature,
              Object value) {
            if (value instanceof Integer) {
              map.put(jarEntry.name - ".class" + name, value)
            }
            return super.visitField(access, name, desc, signature, value)
          }
        }
        classReader.accept(classVisitor, 0)
      }
    }

    jarFile.close()
  }

  static void replaceAndDelRInfo(File jar) {
    File newFile = new File(jar.parentFile, jar.name + ".bak")
    JarFile jarFile = new JarFile(jar)
    new JarOutputStream(new FileOutputStream(newFile)).withStream { OutputStream jarOutputStream ->
      jarFile.entries()
          .grep { JarEntry entry -> entry.name.endsWith(".class") }
          .each { JarEntry entry ->
        jarFile.getInputStream(entry).withStream { InputStream inputStream ->
          ZipEntry zipEntry = new ZipEntry(entry.name)
          def fileBytes = inputStream.bytes

          switch (entry) {
            case { isRFileExceptStyleable(entry.name) }:
              fileBytes = null
              break
            case { isRFile(entry.name) }:
              fileBytes = deleteRInfo(fileBytes)
              break
            default:
              fileBytes = replaceRInfo(fileBytes)
              break
          }

          if (fileBytes != null) {
            jarOutputStream.putNextEntry(zipEntry)
            jarOutputStream.write(fileBytes)
            jarOutputStream.closeEntry()
          }
        }
      }
      jarFile.close()

      jar.delete()
      newFile.renameTo(jar)
    }
  }

  private static byte[] replaceRInfo(byte[] bytes) {
    ClassReader classReader = new ClassReader(bytes)
    ClassWriter classWriter = new ClassWriter(classReader, 0)
    ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM4, classWriter) {
      @Override
      MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {
        def methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
        methodVisitor = new MethodVisitor(Opcodes.ASM4, methodVisitor) {
          @Override
          void visitFieldInsn(int opcode, String owner, String name1, String desc1) {
            Integer constantValue = map.get(owner + name1)
            constantValue != null ? super.visitLdcInsn(constantValue) :
                super.visitFieldInsn(opcode, owner, name1, desc1)
          }
        }
        return methodVisitor
      }
    }
    classReader.accept(classVisitor, 0)

    classWriter.toByteArray()
  }

  private static byte[] deleteRInfo(byte[] fileBytes) {
    ClassReader classReader = new ClassReader(fileBytes)
    ClassWriter classWriter = new ClassWriter(classReader, 0)
    ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM4, classWriter) {
      @Override
      FieldVisitor visitField(int access, String name, String desc, String signature,
          Object value) {
        value instanceof Integer ? null : super.visitField(access, name, desc, signature, value)
      }
    }
    classReader.accept(classVisitor, 0)

    return classWriter.toByteArray()
  }

  /**
   * R file and its sub file
   * @param name file name
   * @return
   */
  private static boolean isRFile(String name) {
    name ==~ '''.*/R\\$.*\\.class|.*/R\\.class'''
  }

  /**
   * all R file and its sub file except R$styleable.class
   * @param name file name
   * @return
   */
  private static boolean isRFileExceptStyleable(String name) {
    name ==~ '''.*/R\\$(?!styleable).*?\\.class|.*/R\\.class'''
  }
}

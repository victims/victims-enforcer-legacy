/*
 * Copyright (C) 2012 Red Hat Inc.
 *
 * This file is part of enforce-victims-rule for the Maven Enforcer Plugin.
 * enforce-victims-rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * enforce-victims-rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with enforce-victims-rule.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.redhat.victims.hash;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A basic reader of Java .class files based of this specification: 
 * @see http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
 * 
 * (A rather sickly port of the Python version that lives in the server)
 *
 * @author gmurphy
 */
public class ClassData {
 
    // u1 readUnsignedByte
    // u2 readUnsignedShort
    // u4 readInt

    /*  
        ClassFile {
            u4 magic;
            u2 minor_version;
            u2 major_version;
            u2 constant_pool_count;
            cp_info constant_pool[constant_pool_count-1];
            u2 access_flags;
            u2 this_class;
            u2 super_class;
            u2 interfaces_count;
            u2 interfaces[interfaces_count];
            u2 fields_count;
            field_info fields[fields_count];
            u2 methods_count;
            method_info methods[methods_count];
            u2 attributes_count;
            attribute_info attributes[attributes_count];
        }
    */
    public byte[] magic;
    public int major_version;
    public int minor_version;
    public cp_info[] constant_pool;
    public int access_flags;
    public int this_class;
    public int super_class;
    public int[] interfaces;
    public field_info[] fields;
    public method_info[] methods;
    public attribute_info[] attributes; 

    private DataInputStream stream;
 
    public ClassData(DataInputStream dis){
        this.stream = dis;
    }

    public DataInputStream getInput(){
        return this.stream;
    }
    public void readAll() throws IOException {
        readMagic();
        readVersion();
        readConstantPool();
        readAccessFlags();
        readThisClass();
        readSuperClass();
        readInterfaces();
        readFields();
        readMethods();
        readAttributes();
    }

    public void readMagic() throws IOException {
        // 0xcafebabe
        magic = new byte[4];
        checkLength(stream.read(magic), magic);
        byte[] cafebabe = { (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe };
        for (int i = 0; i < cafebabe.length; i++){
            if (magic[i] != cafebabe[i])
                throw new IOException("Not a class file");
        }
    }

    public void readVersion() throws IOException { 

        // Java Version
        minor_version = stream.readUnsignedShort();
        major_version = stream.readUnsignedShort();
     
    }

    public void readConstantPool() throws IOException {

        constant_pool = new cp_info[stream.readUnsignedShort()];
        /* constant_pool_length = len(constant_pool) + 1 */
        for (int i = 0; i < constant_pool.length - 1; i++){
            cp_info entry = new cp_info();
            entry.read(stream);
            constant_pool[i] = entry;
        }
    }

    public  void readAccessFlags() throws IOException {
        access_flags = stream.readUnsignedShort();
    }

    public  void readThisClass() throws IOException {
        this_class = stream.readUnsignedShort();
    }
    
    public  void readSuperClass() throws IOException { 
        super_class = stream.readUnsignedShort();
    }

    public  void readInterfaces() throws IOException {
        interfaces = new int[stream.readUnsignedShort()];
        for (int i = 0; i < interfaces.length; i++){
            interfaces[i] = stream.readUnsignedShort();
        }
    }

    public  void readFields() throws IOException { 
        
        fields = new field_info[stream.readUnsignedShort()];
        for (int i = 0; i < fields.length; i++){

            field_info field = new field_info();
            field.read(stream);
            fields[i] = field;
        }
    }

    public  void readMethods() throws IOException {

        methods = new method_info[stream.readUnsignedShort()];
        for (int i = 0; i < methods.length; i++){

            method_info method = new method_info();
            method.read(stream);
            methods[i] = method;
        }
    }

    public  void readAttributes() throws IOException { 
        
        attributes = new attribute_info[stream.readUnsignedShort()];
        for (int i = 0; i < attributes.length; i++){

            attribute_info attribute = new attribute_info();
            attribute.read(stream);
            attributes[i] = attribute;
        }
    }

    /**
     * 
     * @param nread
     * @param data
     * @throws IOException 
     */
    private void checkLength(int nread, byte[] data) throws IOException {
         
         if (nread != data.length){ 
             String err = String.format("Insufficient data read: %d, "
                     + "expected %d. %d available.", 
                     nread, data.length, stream.available());
             throw new IOException(err);
         }
    }

    /**
     * cp_info { 
     *       u1 tag;
     *       u1 info[];
     * }
    */
    public class cp_info {

        public byte tag;
        public byte[] info;

        public void read(DataInputStream stream) throws IOException {

            tag = stream.readByte();

            switch(tag){

                case /* CONSTANT_Class              */ 7:
                case /* CONSTANT_String             */ 8:
                case /* CONSTANT_MethodType         */16:
                    info = new byte[2];
                    break;

                case /* CONSTANT_MethodHandle       */15:
                    info = new byte[3];
                    break;

                case /* CONSTANT_FieldRef           */ 9: 
                case /* CONSTANT_MethodRef          */10:
                case /* CONSTANT_InterfaceMethodref */11:
                case /* CONSTANT_Integer            */ 3: 
                case /* CONSTANT_Float              */ 4:
                case /* CONSTANT_NameAndType        */12: 
                case /* CONSTANT_InvokeDynamic      */18:
                    info = new byte[4];
                    break;

                case /* CONSTANT_Long               */ 5:
                case /* CONSTANT_Double             */ 6:
                    info = new byte[8];
                    break;

                case /* CONSTANT_Utf8               */ 1:
                    int len = stream.readUnsignedShort();
                    info = new byte[len];
                    break;

                default:
                    throw new IOException("Unexpected class tag value: " + tag);
           
            }

            checkLength(stream.read(info), info);
        }
    }

    /**
     * attribute_info {
     *      u2 attribute_name_index
     *      u4 attribute_length
     *      u1 info[attribute_length]
     * }
     */
    public class attribute_info {

        public int attribute_name_index;
        public byte[] info;

        public void read(DataInputStream stream) throws IOException {
            
            attribute_name_index = stream.readUnsignedShort();
            info = new byte[stream.readInt()];
            checkLength(stream.read(info), info);
        } 
    }
  
    /**
     * Common functionality / representation 
     * used in both method_info, and field_info 
     */ 
    public class base_info {
        
        public int flags;
        public int name;
        public int descriptor_index;
        public attribute_info[] attributes;

        public void read(DataInputStream stream) throws IOException {
            
            flags = stream.readUnsignedShort();
            name  = stream.readUnsignedShort();
            descriptor_index = stream.readUnsignedShort();

            attributes = new attribute_info[stream.readUnsignedShort()];
            for (int i = 0; i < attributes.length; i++){
                attribute_info attrib = new attribute_info();
                attrib.read(stream);
                attributes[i] = attrib;
            }
        }
    }

    /**
     * method_info {
     *       u2 access_flags
     *       u2 name_index
     *       u2_descriptor_index
     *       u2 attribute_count
     *       attribute_info attributes[attributes_count] 
     *   }
     */
    public class method_info extends base_info{};
    
    /**
     * field_info {
     *     u2 access_flags
     *      u2 name_index
     *      u2 descriptor_index
     *      u2 attribute_count
     *      attribute_info attributes[attribute_count]
     * }
     */
    public class field_info extends base_info {};

}
   

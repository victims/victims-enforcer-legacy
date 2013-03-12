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
package com.redhat.victims.archive.java;

import com.redhat.victims.archive.ArchiveVisitor;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.ByteSequence;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gmurphy
 */
public class FingerprintClassfile implements ArchiveVisitor {

    private JSONObject fingerprint;
    private String algorithm;

    public FingerprintClassfile(String hashFunction){
        fingerprint = new JSONObject();
        algorithm = hashFunction;
    }



    /**
     * Resolves a constants value from the constant pool to the lowest form
     * it can be represented by. E.g. String, Integer, Float, etc.
     * @param index
     * @param cp
     * @return
     */
    public String constantValue(int index, ConstantPool cp){

        Constant type = cp.getConstant(index);
        if (type != null){
            switch (type.getTag()) {

                case Constants.CONSTANT_Class:
                    ConstantClass cls = (ConstantClass) type;
                    return constantValue(cls.getNameIndex(), cp);


                case Constants.CONSTANT_Double:
                    ConstantDouble dbl = (ConstantDouble) type;
                    return String.valueOf(dbl.getBytes());

                case Constants.CONSTANT_Fieldref:

                    ConstantFieldref fieldRef = (ConstantFieldref) type;
                    return constantValue(fieldRef.getClassIndex(), cp) + " " +
                           constantValue(fieldRef.getNameAndTypeIndex(), cp);

                case Constants.CONSTANT_Float:
                    ConstantFloat flt = (ConstantFloat) type;
                    return String.valueOf(flt.getBytes());

                case Constants.CONSTANT_Integer:
                    ConstantInteger integer = (ConstantInteger) type;
                    return String.valueOf(integer.getBytes());

                case Constants.CONSTANT_InterfaceMethodref:

                    ConstantInterfaceMethodref intRef = (ConstantInterfaceMethodref) type;
                    return constantValue(intRef.getClassIndex(), cp) + " " +
                           constantValue(intRef.getNameAndTypeIndex(), cp);

                case Constants.CONSTANT_Long:

                    ConstantLong lng = (ConstantLong) type;
                    return String.valueOf(lng.getBytes());


                case Constants.CONSTANT_Methodref:
                    ConstantMethodref methRef = (ConstantMethodref) type;
                    return constantValue(methRef.getClassIndex(), cp) + " " +
                           constantValue(methRef.getNameAndTypeIndex(), cp);

                case Constants.CONSTANT_NameAndType:
                    ConstantNameAndType nameType = (ConstantNameAndType) type;
                    return nameType.getName(cp) + " " + nameType.getSignature(cp);

                case Constants.CONSTANT_String:
                    ConstantString str = (ConstantString) type;
                    return str.getBytes(cp);

                case Constants.CONSTANT_Utf8:
                    ConstantUtf8 utf8 = (ConstantUtf8) type;
                    return utf8.getBytes();
            }
        }

        return "";
    }


    /**
     * Normalizes the bytecode using the supplied constant pool. Essentially
     * all lookups via index to the constant pool are resolved and inserted
     * in place with their string value. This is designed to reduce any
     * inconsistencies between the JDK compiled bytecode that is introduced by
     * adding constants at different indices to the constant pool.
     *
     * @param bytes
     * @param cp
     * @return
     * @throws IOException
     */
    public String formatBytecode(ByteSequence bytes, ConstantPool cp) throws IOException {

        StringBuilder buf = new StringBuilder();

        int index;
        byte indexbyte1, indexbyte2;

        short opcode;
        boolean wide = false;

        while (bytes.available() > 0) {

            opcode = (short) bytes.readUnsignedByte();
            buf.append(opcode);


            // adopted from apache bcel codeToString
            switch(opcode){

                case Constants.TABLESWITCH:
                case Constants.LOOKUPSWITCH:
                    throw new IOException("Variable length operations not implemented yet!");

                case Constants.GOTO:
                case Constants.IFEQ:
                case Constants.IFGE:
                case Constants.IFGT:
                case Constants.IFLE:
                case Constants.IFLT:
                case Constants.JSR:
                case Constants.IFNE:
                case Constants.IFNONNULL:
                case Constants.IFNULL:
                case Constants.IF_ACMPEQ:
                case Constants.IF_ACMPNE:
                case Constants.IF_ICMPEQ:
                case Constants.IF_ICMPGE:
                case Constants.IF_ICMPGT:
                case Constants.IF_ICMPLE:
                case Constants.IF_ICMPLT:
                case Constants.IF_ICMPNE:

                    buf.append(bytes.readShort());

                    break;

                case Constants.GOTO_W:
                case Constants.JSR_W:
                    buf.append(bytes.readInt());
                    break;

                case Constants.ALOAD:
                case Constants.ASTORE:
                case Constants.DLOAD:
                case Constants.DSTORE:
                case Constants.FLOAD:
                case Constants.FSTORE:
                case Constants.ILOAD:
                case Constants.ISTORE:
                case Constants.LLOAD:
                case Constants.LSTORE:
                case Constants.NEWARRAY:
                case Constants.RET:
                    if (wide){
                        buf.append(bytes.readUnsignedShort());
                        wide = false;
                    } else {
                        buf.append(bytes.readByte());
                    }
                    break;


                case Constants.WIDE:
                    wide = true;
                    break;

                case Constants.GETFIELD:
                case Constants.GETSTATIC:
                case Constants.PUTFIELD:
                case Constants.PUTSTATIC:
                case Constants.NEW:
                case Constants.CHECKCAST:
                case Constants.INSTANCEOF:
                case Constants.INVOKESPECIAL:
                case Constants.INVOKESTATIC:
                case Constants.INVOKEVIRTUAL:
                case Constants.LDC_W:
                case Constants.LDC2_W:
                case Constants.ANEWARRAY:

                    index = bytes.readUnsignedShort();
                    buf.append(constantValue(index, cp));
                    break;

                case Constants.LDC:
                    index = bytes.readUnsignedByte();
                    buf.append(constantValue(index, cp));
                    break;


                case Constants.MULTIANEWARRAY:

                    index = bytes.readUnsignedShort();

                    buf.append(constantValue(index, cp));
                    buf.append(bytes.readUnsignedByte());
                    break;


                case Constants.IINC:

                    if (wide){
                        buf.append(bytes.readUnsignedShort());
                        buf.append(bytes.readShort());

                    } else {
                        buf.append(bytes.readUnsignedByte());
                        buf.append(bytes.readByte());
                    }
                    break;

                case Constants.INVOKEINTERFACE:

                    index =  bytes.readUnsignedShort();
                    buf.append(constantValue(index, cp));
                    buf.append(bytes.readUnsignedByte());
                    buf.append(bytes.readUnsignedByte());

                    break;

                default:

                    if (Constants.NO_OF_OPERANDS[opcode] > 0){

                        for (int i = 0; i < Constants.TYPE_OF_OPERANDS[opcode].length; i++){

                            switch(Constants.TYPE_OF_OPERANDS[opcode][i]){

                                case Constants.T_BYTE:
                                    buf.append(bytes.readByte());
                                    break;

                                case Constants.T_SHORT:
                                    buf.append(bytes.readShort());
                                    break;

                                case Constants.T_INT:
                                    buf.append(bytes.readInt());
                                    break;

                            }
                        }
                    }
            }
        }

        return buf.toString();
    }



    /**
     * Create a fingerprint for each entry. Currently the compiler version
     * is skipped prior to hashing. It might be more
     * @param name
     * @param entry
     */
    public void visit(String name, InputStream entry) {

        if (name.endsWith(".class")){

            ClassParser parser = new ClassParser(new DataInputStream(entry), name);
            try {

                String ref;
                JavaClass klass = parser.parse();
                MessageDigest md = MessageDigest.getInstance(algorithm);
                ConstantPool cpool = klass.getConstantPool();

                // source file
                md.update(klass.getSourceFileName().getBytes());

                // access flags
                md.update(String.valueOf(klass.getAccessFlags()).getBytes());

                // this class
                ref = constantValue(klass.getClassNameIndex(), cpool);
                md.update(ref.getBytes());


                // super class (extends)
                ref = constantValue(klass.getSuperclassNameIndex(), cpool);
                md.update(ref.getBytes());

                // interfaces (implements)
                try {
                    for (JavaClass jc : klass.getAllInterfaces()){
                        // implemented interface name
                        ref = constantValue(jc.getClassNameIndex(), cpool);
                        md.update(ref.getBytes());
                    }
                } catch (ClassNotFoundException e){
                }

                // fields
                for (Field f : klass.getFields()){

                    // access flags
                    md.update(String.valueOf(f.getAccessFlags()).getBytes());

                    // name
                    ref = constantValue(f.getNameIndex(), cpool);
                    md.update(ref.getBytes());

                    // signature
                    ref = constantValue(f.getSignatureIndex(), cpool);
                    md.update(ref.getBytes());

                    // value
                    if (f.getConstantValue() != null){

                        int index = f.getConstantValue().getConstantValueIndex();
                        ref = constantValue(index, klass.getConstantPool());
                        md.update(ref.getBytes());
                    }
                }

                // methods
                for (Method m : klass.getMethods()){

                    // access flags
                    md.update(String.valueOf(m.getAccessFlags()).getBytes());

                    // name
                    ref = constantValue(m.getNameIndex(), cpool);
                    md.update(ref.getBytes());

                    // signature
                    ref = constantValue(m.getSignatureIndex(), cpool);
                    md.update(ref.getBytes());

                    // code
                    Code code = m.getCode();
                    if (code != null){
                        ByteSequence bytecode = new ByteSequence(code.getCode());
                        String codefmt = formatBytecode(bytecode, cpool);
                        md.update(codefmt.getBytes());
                    }

                }

                String h = new String(Hex.encodeHex(md.digest()));
                fingerprint.put(h, name);


            }
            catch (NoSuchAlgorithmException e){}
            catch (IOException e){}
            catch (JSONException e){}

        }
    }

    public JSONArray results() {
        JSONArray res =  new JSONArray();
        res.put(fingerprint);
        return res;
    }
}

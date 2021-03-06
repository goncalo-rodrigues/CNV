//
// StatisticsToolToFile.java
//
// This program measures and instruments to obtain different statistics
// about Java programs.
//
// Copyright (c) 1998 by Han B. Lee (hanlee@cs.colorado.edu).
// ALL RIGHTS RESERVED.
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for non-commercial purposes is hereby granted provided
// that this copyright notice appears in all copies.
//
// This software is provided "as is".  The licensor makes no warrenties, either
// expressed or implied, about its correctness or performance.  The licensor
// shall not be liable for any damages suffered as a result of using
// and modifying this software.

import BIT.highBIT.*;
import BIT.lowBIT.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;

public class StatisticsFunctionCallsToFile
{
	private static HashMap<Long, MethodInvocationTree> dyn = new HashMap<Long, MethodInvocationTree>();
	private static HashMap<Long, Boolean> readingScene = new HashMap<Long, Boolean>();

	public static void printUsage()
		{
			System.out.println("Syntax: java StatisticsToolToFile -stat_type in_path [out_path]");
			System.out.println("        where stat_type can be:");
			System.out.println("        static:     static properties");
			System.out.println("        dynamic:    dynamic properties");
			System.out.println("        alloc:      memory allocation instructions");
			System.out.println("        load_store: loads and stores (both field and regular)");
			System.out.println("        branch:     gathers branch outcome statistics");
			System.out.println();
			System.out.println("        in_path:  directory from which the class files are read");
			System.out.println("        out_path: directory to which the class files are written");
			System.out.println("        Both in_path and out_path are required unless stat_type is static");
			System.out.println("        in which case only in_path is required");
			System.exit(-1);
		}

	public static void doDynamic(File in_dir, File out_dir)
		{
			String filelist[] = in_dir.list();

			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i];
				if (filename.endsWith(".class")) {
					String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
					ClassInfo ci = new ClassInfo(in_filename);
//					Cp_Info[] cpool = ci.getConstantPool();

					// Each routine
					for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
						Routine routine = (Routine) e.nextElement();

						routine.addBefore("StatisticsFunctionCallsToFile", "callMethod", routine.getMethodName());
						routine.addAfter("StatisticsFunctionCallsToFile", "retMethod", routine.getMethodName());

						Instruction[] instructions = routine.getInstructions();
						for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
							BasicBlock bb = (BasicBlock) b.nextElement();
							Instruction instr = (Instruction) instructions[bb.getEndAddress()];

							if (instr.getOpcode() == InstructionTable.invokevirtual ||
									instr.getOpcode() == InstructionTable.invokestatic ||
									instr.getOpcode() == InstructionTable.invokespecial) {

//								CONSTANT_Methodref_Info cminfo = (CONSTANT_Methodref_Info) cpool[instr.getOperandValue()];
//								CONSTANT_NameAndType_Info ntinfo = (CONSTANT_NameAndType_Info) cpool[cminfo.name_and_type_index];
//								CONSTANT_Utf8_Info nameinfo = (CONSTANT_Utf8_Info) cpool[ntinfo.name_index];
//								String callee = new String(nameinfo.bytes);
//								if (callee.equals("<init>")) {
//									CONSTANT_Class_Info classInfo = (CONSTANT_Class_Info) cpool[cminfo.class_index];
//									CONSTANT_Utf8_Info classnameinfo = (CONSTANT_Utf8_Info) cpool[classInfo.name_index];
//									callee = new String(classnameinfo.bytes) + "" + callee;
//								}
//
//								instr.addBefore("StatisticsToolToFile", "dynCallCount", caller + "»" + callee);

								instr.addBefore("StatisticsFunctionCallsToFile", "incInst", "null");
							}
						}
					}

					if(filename.equals("Main.class"))
						ci.addAfter("StatisticsFunctionCallsToFile", "printTree", "null");

					ci.write(out_filename);
				}
			}
		}

    public static synchronized void printTree(String foo)
		{
			/*
			 *  Changed the way of outputting the information
			 */

			long threadId = Thread.currentThread().getId();
			MethodInvocationTree mit = dyn.get(threadId);
			dyn.remove(threadId);

			try{
				PrintWriter writer = new PrintWriter("dynamic_" + threadId + ".txt", "UTF-8");

				mit.print(writer);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	public static void incInst(String something)
		{
			long threadId = Thread.currentThread().getId();
			dyn.get(threadId).incCurrentMethod();
		}

    public static void callMethod(String name)
		{
			if(name.equals("<clinit>") || name.equals("<init>"))
				return;

			long threadId = Thread.currentThread().getId();

			synchronized (dyn) {
				if (!dyn.containsKey(threadId)) {
					dyn.put(threadId, new MethodInvocationTree(name));
					readingScene.put(threadId, false);
					return;
				}
			}

			if(name.equals("readScene"))
				readingScene.put(threadId, true);

			if(readingScene.get(threadId))
				return;

			MethodInvocationTree mit = dyn.get(threadId);
			mit.call(name);
		}

	public static void retMethod(String name)
		{
			if(name.equals("<clinit>") || name.equals("<init>"))
				return;

			long threadId = Thread.currentThread().getId();

			if(readingScene.get(threadId)) {
				if (name.equals("readScene"))
					readingScene.put(threadId, false);
				return;
			}

			MethodInvocationTree mit = dyn.get(threadId);
			mit.ret();
		}

	public static void main(String argv[])
		{
			if (argv.length < 2 || !argv[0].startsWith("-")) {
				printUsage();
			}

			else if (argv[0].equals("-dynamic")) {
				if (argv.length != 3) {
					printUsage();
				}

				try {
					File in_dir = new File(argv[1]);
					File out_dir = new File(argv[2]);

					if (in_dir.isDirectory() && out_dir.isDirectory()) {
						doDynamic(in_dir, out_dir);
					}
					else {
						printUsage();
					}
				}
				catch (NullPointerException e) {
					printUsage();
				}
			}
		}
}

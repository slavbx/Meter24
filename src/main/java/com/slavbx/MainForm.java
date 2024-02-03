package com.slavbx;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class MainForm {
    private JButton button1;
    private JTextField textFieldOutput;
    private JPanel jpanel;
    private JTextField textField2;
    private JButton button2;
    private JTextField textField3;
    private JTextField textFieldInput;
    private JTextArea textArea1;
    private JTextArea textArea2;
    byte[] bytesOrig;
    byte[] bytesOdo = new byte[17 * 2];
    int sumNew;
    int[] pair = new int[17];
//    String filePath = "dump.bin";
//    String filePathNew = "dump_new.bin";
    String filePath;
    String filePathNew;
    int sum = -1;


    public MainForm() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filePath = textFieldInput.getText();
                filePathNew = textFieldOutput.getText();

                System.out.println(filePath);
                System.out.println(filePathNew);
                textArea1.setText("");
                try {
                    bytesOrig = Files.readAllBytes(Paths.get(filePath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
                //Собираем текущие показания пробега. Для этого нужно просуммировать 17 двухбайтовых int значений
                //Байты переворачиваем, не забываем, что byte знаковый
                sum = 0;
                for (int i = 0; i < 17 * 2; i = i + 2) {
                    pair[i / 2] = ((bytesOrig[i + 1] & 0xFF)  << 8) | bytesOrig[i] & 0xFF;
                }
                //Суммируем пары для подсчёта текущего пробега
                for (int i = 0; i < 17; i++) {
                    sum = sum + pair[i];
                }
                textField2.setText(Integer.toString(sum));

                //Отображение считанных байт
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < 17 * 2; i++) {
                    s.append(String.format("%04X", bytesOrig[i]).substring(2)).append(" ");
                }
                textArea1.setText(s.toString());

                //Если корректно считан пробег, открываем кнопку записи
                if (sum >= 0 && sum <= 1114095) {
                    button2.setEnabled(true);
                }


            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filePath = textFieldInput.getText();
                filePathNew = textFieldOutput.getText();
                //Проверяем введенное число
                if (textField3.getText().equals("")) {
                    sumNew = 0;
                } else {
                    sumNew = Integer.parseInt(textField3.getText());
                }
                //Если новый пробег в диапазоне, начинаем запись
                if (sumNew >= 0 && sum <= 1114095) {
                    //Записываем базовый пробег, который делится на 17
                    int base = sumNew / 17;

                    //Формируем байты базового пробега
                    for (int i = 0; i < 17 * 2; i = i + 2) {
                        bytesOdo[i] = (byte) base;
                        bytesOdo[i + 1] = (byte) (base >> 8);
                    }
                    //Увеличиваем младшие байты по порядку на остаток от деления на 17. Число байт и есть остаток
                    int rem = sumNew - base * 17;
                    for (int i = 0; i < rem * 2; i = i + 2) {
                        bytesOdo[i]++;
                    }
                    //Записываем сначала в массив
                    byte[] bytesNew = new byte[bytesOrig.length];
                    for (int i = 0; i < bytesNew.length; i++) {
                        bytesNew[i] = bytesOrig[i];
                    }

                    for (int i = 0; i < bytesOdo.length; i++) {
                        bytesNew[i] = bytesOdo[i];
                    }
                    try {
                        if (!Files.exists(Paths.get(filePathNew))) {
                            Files.createFile(Paths.get(filePathNew));
                        }
                        Files.write(Paths.get(filePathNew), bytesNew);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    //Отображение считанных байт
                    StringBuilder s = new StringBuilder();
                    for (int i = 0; i < bytesOdo.length; i++) {
                        s.append(String.format("%04X", bytesOdo[i]).substring(2)).append(" ");
                    }
                    textArea2.setText(s.toString());

                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame jframe = new JFrame("Meter24");
        jframe.setLocationRelativeTo(null);
        jframe.setContentPane(new MainForm().jpanel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.pack();
        jframe.setVisible(true);
    }
}

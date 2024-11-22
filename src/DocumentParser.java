import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;


import Model.Element;
import Model.ElementAndSegmentContainer;
import Model.Segment;


import java.text.Normalizer;



/*
 * 
 *  Author: Gustavo Nakasone
 *  Converte documento SAP para TRN
 * 
 * necessário definir:
 *      filePath
 *      IDOC Report File
 * 
 */
public class DocumentParser {

    public static void main(String[] args) {
        String projectPath = new File("").getAbsolutePath();
        String filePath = projectPath + "\\src\\Files\\";
        String idocReportFile = "";
        String DocReadPath = filePath+ ".." + "\\DocRead\\";
        String TRNPath = filePath + "\\TRN\\";

        setupFile(filePath);
        // Caminho do diretório
        String DOCSAP = System.getProperty("user.dir") + "\\src\\Files\\DOCSAP\\";
        
        // Criação de um objeto File representando o diretório
        File diretorio = new File(DOCSAP);
        
        // Verifica se o caminho é um diretório e lista os arquivos
        if (diretorio.isDirectory()) {
            File[] arquivos = diretorio.listFiles();
            if (arquivos != null && arquivos.length > 0) {
                for (File arquivo : arquivos) {
                    // Imprime o nome de cada arquivo encontrado no diretório
                    idocReportFile = filePath + "\\DOCSAP\\" + arquivo.getName();
                    System.out.println("Arquivo encontrado: " + arquivo.getName());
                }
            } else {
                System.out.println("O diretório está vazio ou não há arquivos.");
                return;
            }
        } else {
            System.out.println("O caminho especificado não é um diretório.");
            return;
        }

        setupFile(DocReadPath);
        setupFile(TRNPath);
        

        
        

        ElementAndSegmentContainer container = readDocument(idocReportFile);
        List<Element> elements = container.getElements();
        List<Segment> segmentStructure = container.getsegmentStructure();
        List<Segment> segments = container.getSegments();
        String IDOCTYPE = container.getIDOCTYPE();
        String IDOCDESC = "IDOC " + IDOCTYPE + " para interface com SAP";
    
        // realiza o ajuste na nomenclatura dos campos quando exceder 8 caracteres
        elements = prep_elements(elements);
        for (Segment segment : segments) {
            segment.setElements(prep_elements(segment.getElements()));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DocReadPath + IDOCTYPE +"_ELEMENTS.txt", StandardCharsets.UTF_8))){
            String Arq = "";
            
            for (Element element : elements) {
                Arq = Arq + "NAME :" + element.getfieldName() + " TEXT :" + element.getdescription() + " TYPE :" + element.getType() + " LENGTH :" + element.getLength() + "\r\n" ;
            }
            writer.write(Arq);
            System.out.println("Conteúdo ELEMENTS gravado com sucesso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DocReadPath + IDOCTYPE + "_SEGMENTS.txt", StandardCharsets.UTF_8))){
            String Arq = "";
            
            for (Segment segment : segments) {
                Arq = Arq + "SEGMENT NAME: " + segment.getsegmentName() + "| LEVEL: " + segment.getLevel() + "| STATUS: " + segment.getstatus() + "| LOOPMAX: " + segment.getLoopMax() + "| TYPE OF SEG: " + segment.gettype() + "| SeqSegment: " + segment.getseq() +  "\r\n"  ;
                
                for (Element sElement : segment.getElements()) {
                    Arq = Arq + "Field: " + sElement.getfieldName() + "| LENGTH " + sElement.getLength() + "| FIELD SEQ:" + sElement.getfieldSeq() + "\r";
                }
            }
            writer.write(Arq);
            System.out.println("Conteúdo SEGMENTS gravado com sucesso.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DocReadPath + IDOCTYPE +"_STRUCTURE.txt", StandardCharsets.UTF_8))){
            String Arq = "";
            
            for (Segment segment : segmentStructure) {
                Arq = Arq + "SEGMENT NAME: " + segment.getsegmentName() + "| LEVEL: " + segment.getLevel() + "| STATUS: " + segment.getstatus() + "| LOOPMAX: " + segment.getLoopMax() + "| TYPE OF SEG: " + segment.gettype() + "| SeqSegment: " + segment.getseq() +  "\r\n"  ;
            }
            writer.write(Arq);
            System.out.println("Conteúdo STRUCTURE gravado com sucesso.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        genTRN(TRNPath ,DocReadPath + IDOCTYPE, elements, segments, segmentStructure, "01", IDOCTYPE, IDOCDESC);

    
    }
    public static void setupFile(String filePath) {
        // Cria um objeto File com o caminho da pasta
        File file = new File(filePath);

        // Verifica se a pasta existe
        if (!file.exists()) {
            // Cria a pasta (ou as pastas) caso não existam
            if (file.mkdirs()) {
                System.out.println("Pasta criada com sucesso: " + filePath);
            } else {
                System.out.println("Falha ao criar a pasta: " + filePath);
            }
        } else {
            System.out.println("A pasta já existe: " + filePath);
        }
    }
    
    public static String removerCaracteresEspeciais(String texto) {
        // Normaliza o texto para decompor caracteres especiais
        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // Remove caracteres não ASCII
        String textoSemAcentos = textoNormalizado.replaceAll("[^\\p{ASCII}]", "");
        return textoSemAcentos;
    }
    
    public static ElementAndSegmentContainer readDocument(String filePath) {
        String IDOCTYPE = "";
        
        String At_TypeObject = "none";
        List<Element> elements = new ArrayList<>();
        int lineNum = 0;
        // Field variables
        String fieldName = "";
        String description = "";
        String type = "";
        int length = 0;
        Element dummy = null;

        //Segment List
        List<Segment> segmentsList = new ArrayList<>();

        //Segment variables
        String nomeSeg = "";
        int level = 1;
        String status = "";
        int loopMax = 1;
        int segmentSeq = 0;
        int fieldSeq = 100;
        boolean geraLoop = false;
        int depthLoop = 1;
        int prev_level = 1;
        List<Segment> segments = new ArrayList<>();
        List<Element> seqElements = new ArrayList<>();
        Segment segment = null;

        //Groups


        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String linha;
         
            

            while ((linha = reader.readLine()) != null) {
                // Processa cada linha do arquivo aqui
                lineNum += 1;
                String[] partes = linha.trim().split("\\s+");

                String chave;
                String valor;

                if (partes.length >= 2) {
                    chave = partes[0];
                    valor = partes[1];
                } else if (partes.length == 1) {
                    chave = partes[0];
                    valor = ""; // Pode ser um valor nulo ou uma string vazia
                } else {
                    continue; // Pula linhas que não possuem informação suficiente
                }
                
                //Procura pelo Type Field (Elementos) não necessáriamente obrigatório para o funcionamento
                if (chave.contains("BEGIN_FIELDS")) {
                    At_TypeObject = "Field";
                    
                    continue;
                } 
                else if (chave.contains("END_FIELDS")){
                    At_TypeObject = "none";
                    continue;
                }
                if (chave.contains("BEGIN_SEGMENT")) {
                    At_TypeObject = "Segment";
                    fieldSeq = 100;
                    seqElements = new ArrayList<>();
                    continue;
                }
                else if (chave.contains("END_SEGMENT")){
                    segmentSeq += 10;
                    
                    if (geraLoop){
                        geraLoop = false;
                        segment = new Segment(nomeSeg, depthLoop-1, status, loopMax, null, segmentSeq, "L");
                        segments.add(segment);
                        segment = new Segment(nomeSeg, depthLoop, status, loopMax, new ArrayList<Element>(seqElements), segmentSeq, "S");
                        
                        segments.add(segment);
                        segmentsList.add(segment);
                    }
                    else
                    {
                        segment = new Segment(nomeSeg, depthLoop, status, loopMax, new ArrayList<Element>(seqElements), segmentSeq, "S");
                        
                        segments.add(segment);
                        segmentsList.add(segment);
                    }
                    
                    
                    
                    At_TypeObject = "none";
                    
                    

                    continue;
                }
                if (chave.contains("BEGIN_GROUP")) {
                    geraLoop = true;
                    depthLoop +=1;
                    continue;
                } 
                else if (chave.contains("END_GROUP")) {
                    geraLoop = false;
                    depthLoop -=1;
                    continue;
                } 
                else if (chave.contains("BEGIN_IDOC")){
                    IDOCTYPE = valor;
                }
                //Inicializa o mapeamento dos campos
                if (At_TypeObject.equals("Field")) {

                    if (chave.contains("NAME")) {fieldName = removerCaracteresEspeciais(valor.trim());dummy = null;}
                    else if (chave.contains("TEXT")) {description = removerCaracteresEspeciais(valor.trim());}
                    else if (chave.contains("TYPE")) {type = removerCaracteresEspeciais(valor.trim());}
                    else if (chave.contains("LENGTH")) {
                        length = Integer.parseInt(valor.trim()); 
                        if (!(fieldName.isEmpty()) && !(description.isEmpty()) && !(type.isEmpty()) && length > 0 ){
                            
                            
                            dummy = new Element(fieldName, description, type, length);
                            // Não gera, se valor do campo FieldName e Length já existirem na lista
                            boolean foundEqual = false;
                            
                            if (elements.isEmpty()) {
                                fieldSeq += 10;
                                dummy.setfieldSeq(fieldSeq);
                                
                                //dummy.setfieldName(dummy.getfieldName().substring(0,8));
                                elements.add(dummy); 
                                seqElements.add(dummy);
                            } 
                            else {
                                for (Element element : elements) {
                                    if (dummy.getfieldName().equals(element.getfieldName()) && dummy.getLength() == element.getLength()) {
                                        foundEqual = true;
                                        break;
                                    }
                                }
                                if (!foundEqual) {
                                    fieldSeq += 10;
                                    elements.add(dummy);
                                    
                                    dummy.setfieldSeq(fieldSeq);
                                    seqElements.add(dummy);
                                }
                                else{
                                    fieldSeq += 10;
                                    dummy.setfieldSeq(fieldSeq);
                                    seqElements.add(dummy);
                                }
                            }
                            

                            /* LOG PARA DEBUG --> Segmento
                            System.out.println("Created Element");
                            System.out.println("Field Name: " + fieldName);
                            System.out.println("TEXT      : " + description);
                            System.out.println("type      : " + type);
                            System.out.println("LENGTH    : " + length);
                            System.out.println("=======================================");
                            */
                            
                        }
                        else { 
                            System.out.println("Erro, não foi possível criar a variavel tipo Element. Verificar as informações da linha:" + (lineNum > 4 ? lineNum-4 : lineNum));
                            System.out.println("Field Name: " + fieldName);
                            System.out.println("TEXT      : " + description);
                            System.out.println("type      : " + type);
                            System.out.println("LENGTH    : " + length);
                        }

                    }

                } 
                if (At_TypeObject.equals("Segment")) {
                    if (chave.contains("SEGMENTTYPE")){nomeSeg = removerCaracteresEspeciais(valor.trim());}
                    else if (chave.contains("LEVEL")){level = Integer.parseInt(valor.trim());}
                    else if (chave.contains("STATUS")){status = removerCaracteresEspeciais(valor.trim());}
                    else if (chave.contains("LOOPMAX")){loopMax = Integer.parseInt(valor.trim());}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        return new ElementAndSegmentContainer(IDOCTYPE, elements, segments, segmentsList);
    }

    
    public static void genTRN (String TRNPath, String fileDocRead, List<Element> Elements, List<Segment> Segments, List<Segment> Structure, String version, String IDOCTYPE, String IDOCDESC) {
        String DocumentTRN = "";
        int lineCount = 1;
        DocumentTRN = "BEG^6^1^"+DataAtual()+"^0^Y~\r\n";  
        lineCount += 1;
        String EDI_DC_Version = "4.0"; // Padronizado 63 posições 
        String StartDOC = "_"; // Relação entre o Wrapper e o Documento
        String SegmentStructure = ""; // Para geração do Wrapper preciso primeiramente pegar a informação do segmento
        String Wrapper;
        DocumentTRN = DocumentTRN + "TRNHDR^"+IDOCTYPE+"^" + version +  "^"+ IDOCTYPE +"^"+ IDOCDESC +"^^^APPL\\"+IDOCTYPE+".TXT^APP."+IDOCTYPE+"^^False^D^^~\r\n";  
        lineCount += 1;

        for (Segment StructureLine : Structure) {
            String Area = "1"; // Defini sempre como 1 para não perder a referencia na hora de criar os segmentos (Sempre será criado na ordem e no nivel gerado pelo BEGIN_GROUP)
            int Seq = StructureLine.getseq();
            String Type = StructureLine.gettype();
            String SegName = StructureLine.getsegmentName();
            String Required = StructureLine.getstatus() == "MANDATORY" ? "M" : "O";
            
            int LoopLevel = StructureLine.getLevel();
            int LoopMax = StructureLine.getLoopMax();
            DocumentTRN = DocumentTRN + "TRNDTL^"+ 
                                        Area + "^" + 
                                        Seq + "^" +
                                        Type + "^" +
                                        SegName + "^" +
                                        Required + "^0^" +
                                        LoopMax + "^" +
                                        LoopLevel + "^~\r\n"
                                        ; 
                                        lineCount += 1;

            if (StartDOC.equals("_")) {//Pega o primeiro segmento para relacionar como Wrapper
                StartDOC = SegName;
            }
        }

        /*
         *
         * Gerando WRAPPER (Somente o WRAPHDR/WRAPDTL)
         * 
         */
        
        DocumentTRN = DocumentTRN + genWrapper(IDOCTYPE, version, EDI_DC_Version, StartDOC); // 4.0 Fará a geração do IDOC com 64 posições
        lineCount += 2;

        for (Segment segment : Segments) {
            
            DocumentTRN = DocumentTRN + "SEGHDR^"+IDOCTYPE+"^" + version + "^" + segment.getsegmentName() + "^"+ "Segmento " + segment.getsegmentName() + "^^^^~\r\n"; 
            lineCount += 1;
            DocumentTRN = DocumentTRN + genPatternIDOC(IDOCTYPE, version, EDI_DC_Version);
            for (Element segFields : segment.getElements()) {
                DocumentTRN = DocumentTRN + "SEGDTL^" + segFields.getfieldSeq() +"^"+ segFields.getfieldName() + "^O^^^~\r\n"; 
                lineCount += 1;
            }
        }

        DocumentTRN = DocumentTRN + genWrapperSegments(IDOCTYPE, version, EDI_DC_Version);


        DocumentTRN = DocumentTRN + genWrapperElements(IDOCTYPE, version, EDI_DC_Version);
        for (Element element : Elements) {
            
            DocumentTRN = DocumentTRN + "ELEHDR^" + 
                            IDOCTYPE + "^" + 
                            version + "^" + 
                            element.getfieldName() + "^" + 
                            element.getdescription() + "^^^" + 
                            (element.getType().trim().equals("CHARACTER") ? "A" : "N") +
                            "^1^" + // Minimo de caracteres (deixei fixo 1)
                            element.getLength() + 
                            "^E~\r\n"; //Acredito que seja 'E'de Elements
                            lineCount += 1;
        }

        //STDTYPE
        DocumentTRN = DocumentTRN + "STDDEF^"+IDOCTYPE+"^"+ version +"^"+ IDOCTYPE+"^^F^\\x0A\\x0D\\x20\\x09\\x00~\r\n";
        DocumentTRN = DocumentTRN + "STDTYPE^A^1^\"\\x00-\\xff\"^~\r\n" + 
                                    "STDTYPE^D^5^\"0-9\"^yyyymmdd~\r\n" + 
                                    "STDTYPE^N^2^\"0-9\"^~\r\n";
                                    lineCount += 4;

        DocumentTRN = DocumentTRN + "SEP^====================End of Detail Section===================~\r\n";
        lineCount += 1;
        DocumentTRN = DocumentTRN + "TRNHDR^"+IDOCTYPE+"^"+ version + "^"+ IDOCTYPE + "^" + IDOCDESC + "~\r\n";
        lineCount += 1;

        DocumentTRN = DocumentTRN + "STDDEF^"+IDOCTYPE+"^" + version + "^" + IDOCDESC + "~\r\n";
        lineCount += 1;

        DocumentTRN = DocumentTRN + "END^" + lineCount + "^"+ DataAtual() + "~";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRNPath + IDOCTYPE + "_" +version + "_STRUCTURE.trn", StandardCharsets.UTF_8))){
            String Arq = DocumentTRN;
            
            writer.write(Arq);
            System.out.println("Conteúdo STRUCTURE gravado com sucesso.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        
    }

    public static String DataAtual(){
        LocalDateTime dataAtual = LocalDateTime.now(); // Define o formato desejado 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd^HHmmss"); // Formata a data return
        return dataAtual.format(formatter);
    }

    public static List<Element> prep_elements(List<Element> elements){
        for (Element element : elements) {
            if (element.getfieldName().length() > 8){
                
                element.setfieldName(newElement(elements, element).getfieldName());
                
            }

        }
        return elements;
    }

    public static Element newElement (List<Element> elements, Element element_new){
        int newID = 0;
        Element aux = new Element(element_new.getfieldName().substring(0, 8), 
                                  element_new.getdescription(), 
                                  element_new.getType(), 
                                  element_new.getLength(), element_new.getfieldSeq());

        
        while (newID != -1){
            if (!elements.contains(aux)){
                newID++;
                aux.setfieldName(aux.getfieldName().substring(0,7) + String.valueOf(newID));
                break;
            }
        }
        
        return aux;
    }

    
    public static String genWrapper (String IDOCTYPE, String Version, String EDI_DC_Version, String RelatedSeg){
        String wrapper = "";
        if (EDI_DC_Version.equals("4.0")){
            //WRAPHDR
            wrapper = wrapper + "WRAPHDR^"+ IDOCTYPE +"^"+ Version + "^EDI_DC40^Cabeçalho de IDOC - Release 4.0^F^^^^^^^0^^^^^^^^appl/EDI_DC40.txt^APP.EDI_DC40^"+ RelatedSeg +"^^T^^^False^~\r\n";
            
            //WRAPDTL
            wrapper = wrapper + "WRAPDTL^1^10^^EDI_DC^M^^0^1~\r\n";
            
        }

        return wrapper;
    }

    public static String genWrapperSegments (String IDOCTYPE, String Version, String EDI_DC_Version){
        String WrapperSeg = "";
        if (EDI_DC_Version.equals("4.0")){
            //SEGHDR
            WrapperSeg = WrapperSeg + "SEGHDR^"+ IDOCTYPE +"^"+ Version + "^EDI_DC^Header de mensagem IDOC^^F^^~\r\n";
            
            

            //SEGDTL
            WrapperSeg = WrapperSeg + """
SEGDTL^10^0001^M^22^0^~
SEGDTL^20^C0001^O^^0^~
SEGDTL^30^MANDT^O^^0^~
SEGDTL^40^DOCNUM^O^0^0^~
SEGDTL^50^DOCREL^O^^0^~
SEGDTL^60^STATUS^O^^0^~
SEGDTL^70^DIRECT^O^0^0^~
SEGDTL^80^OUTMOD^O^^0^~
SEGDTL^90^EXPRSS^O^^0^~
SEGDTL^100^TEST^O^^0^~
SEGDTL^110^IDOCTY^O^0^0^~
SEGDTL^120^CIMTYP^O^^0^~
SEGDTL^130^MESTYP^O^^0^~
SEGDTL^140^MESCOD^O^^0^~
SEGDTL^150^MESFCT^O^^0^~
SEGDTL^160^STD^O^^0^~
SEGDTL^170^STDVRS^O^^0^~
SEGDTL^180^STDMES^O^^0^~
SEGDTL^190^SNDPOR^O^^0^~
SEGDTL^200^SNDPRT^O^^0^~
SEGDTL^210^SNDPFC^O^^0^~
SEGDTL^220^SNDPRN^O^1^0^~
SEGDTL^230^SNDSAD^O^0^0^~
SEGDTL^240^SNDLAD^O^^0^~
SEGDTL^250^RCVPOR^O^^0^~
SEGDTL^260^RCVPRT^O^^0^~
SEGDTL^270^RCVPFC^O^^0^~
SEGDTL^280^RCVPRN^O^4^0^~
SEGDTL^290^RCVSAD^O^^0^~
SEGDTL^300^RCVLAD^O^0^0^~
SEGDTL^310^CREDAT^O^12^0^~
SEGDTL^320^CRETIM^O^13^0^~
SEGDTL^330^REFINT^O^0^0^~
SEGDTL^340^REFGRP^O^0^0^~
SEGDTL^350^REFMES^O^^0^~
SEGDTL^360^ARCKEY^O^^0^~
SEGDTL^370^SERIAL^O^^0^~
            """;
            
        }

        return WrapperSeg;
    }

    public static String genWrapperElements (String IDOCTYPE, String Version, String EDI_DC_Version){
        String wrapperElements = "";

        if (EDI_DC_Version.equals("4.0")){
            wrapperElements = "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^0001^Nome do segmento^^^A^6^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^0023^Filler 23^^^A^1^23^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^MANDT^Mandante^^^A^1^3^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^C0001^Complemento do EDI_DC^F^^A^1^4^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^DOCNUM^Número documento intermediário^^^A^1^16^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^DOCREL^Release SAP do documento intermediário^^^A^1^4^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^STATUS^Status do documento intermediário^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^DIRECT^Direção^^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^F001^Filler de 1 posicao^F^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^OUTMOD^Modo de saída^^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^EXPRSS^Overriding in inbound processing^^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^TEST^Opção de teste^^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^IDOCTY^Tipo de IDOC básico^^^A^1^30^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^CIMTYP^Nome do tipo de extensão^^^A^1^30^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^MESTYP^Nome da mensagem lógica^^^A^1^30^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^MESCOD^Variante lógica de mensagem^^^A^1^3^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^MESFCT^Função lógica de mensagem^^^A^1^3^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SEGNAM^Complemento do Nome do Segmento^F^^A^1^6^E~\r\n"+
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SEGNUM^Número segmento SAP^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^PSGNUM^Número do segmento SAP superior^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^HLEVEL^Nível de hierarquia do segmento SAP^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^STD^Padrão EDI^^^A^1^1^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^STDVRS^Versão do padrão EDI^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^STDMES^Categoria de mensagem EDI^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^STDMES^Categoria de mensagem EDI^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDPOR^Porta do emissor (Sist. SAP, subsist. EDI)^^^A^1^10^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDPRT^Tipo de parceiro do emissor^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDPFC^Função do parceiro do emissor^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDPRN^Número parceiro do emissor^^^A^1^10^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDSAD^EDI: SADR fields in total^^^A^1^21^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SNDLAD^Endereço lógico do emissor^^^A^1^70^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVPOR^Porta do receptor (sist. SAP, subsist. EDI)^^^A^1^10^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVPRT^Tipo de parceiro do receptor^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVPFC^Função de parceiro do receptor^^^A^1^2^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVPRN^Número parceiro do receptor^^^A^1^10^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVSAD^EDI: SADR  fields in total^^^A^1^21^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^RCVLAD^Endereço lógico do receptor^^^A^1^70^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^CREDAT^Data da criação do documento intermediário^^^A^1^8^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^CRETIM^Hora da criação do documento intermediário^^^A^1^6^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^REFINT^Referência ao file de transferência^^^A^1^14^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^REFGRP^Referência ao grupo de mensagens^^^A^1^14^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^REFMES^Referência a mensagem^^^A^1^14^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^ARCKEY^Chave do arquivo EDI^^^A^1^70^E~\r\n" +
                              "ELEHDR^"+ IDOCTYPE +"^"+ Version +"^SERIAL^EDI/ALE: campo de serialização^^^A^1^20^E~\r\n";

        }

        return wrapperElements;
    }

    public static String genPatternIDOC (String IDOCTYPE, String Version, String EDI_DC_Version){
        String SegBeginningStruct ="";
        if (EDI_DC_Version.equals("4.0")){
            SegBeginningStruct = """
SEGDTL^1^F001^O^^0^~
SEGDTL^2^SEGNAM^M^^T^~
SEGDTL^3^0023^M^^^~
SEGDTL^4^MANDT^O^^0^~
SEGDTL^5^DOCNUM^O^^0^~
SEGDTL^6^SEGNUM^O^^0^~
SEGDTL^7^PSGNUM^O^^0^~
SEGDTL^8^HLEVEL^O^^0^~
                    """;
        }

        return SegBeginningStruct;
    }
}


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VacanciesSearcher {
    private static final String URL_FORMAT = "http://hh.ua/search/vacancy?text=%s+%s+&page=%d";
    private static final String userAgent = "Chrome/56.0.2924.87";
    private static final String referrer = "https://hh.ua/";

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Vacancies Searcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1370,760);
        frame.setLayout(new GridBagLayout());

        JLabel professionLabel = new JLabel("Profession");
        JLabel cityLabel = new JLabel("City");
        JTextField professionTextField = new JTextField(17);
        JTextField cityTextField = new JTextField(17);
        JButton button = new JButton("Search!");
        JTextArea textArea = new JTextArea(20,15);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = professionTextField.getText();
                String q = cityTextField.getText();
                List<Vacancy>list = new VacanciesSearcher().getVacancies(s,q);
                StringBuilder stringBuilder = new StringBuilder();
                int i = 1;
                for (Vacancy v:list) {
                    stringBuilder.append(i);
                    stringBuilder.append(" ");
                    stringBuilder.append(v.toString());
                    stringBuilder.append("\n");
                    stringBuilder.append("\n");
                    i++;
                }
                textArea.setText(stringBuilder.toString()+"\n"+"Total amount of vacancies is: "+list.size());
            }
        });

        textArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        frame.add(professionLabel,new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,0,2),0,0));

        frame.add(professionTextField,new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,0,900),0,0));

        frame.add(cityLabel,new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,0,2),0,0));

        frame.add(cityTextField,new GridBagConstraints(1,1,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,0,900),0,0));

        frame.add(button,new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,0,2),0,0));

        frame.add(scrollPane,new GridBagConstraints(0,3,2,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,
                new Insets(2,2,2,2),0,0));

        frame.setVisible(true);
    }

    protected Document getDocument(String searchString, String searchString2, int page) throws IOException{
        String url = String.format(URL_FORMAT,searchString, searchString2, page);
        Document document = Jsoup.connect(url).userAgent(userAgent).timeout(60*1000).referrer(referrer).get();
        return document;
    }

    public List<Vacancy> getVacancies(String searchString, String searchString2) {
        List<Vacancy> vacancies= new ArrayList<>();
        try {
            int pageNumber = 0;
            Document document;
            do {
                document = getDocument(searchString, searchString2, pageNumber++);
                if (document==null) break;
                Elements elements = document.getElementsByAttributeValue("data-qa", "vacancy-serp__vacancy");
                if (!elements.isEmpty()) {
                    for (Element element : elements) {
                        Vacancy vacancy = new Vacancy();
                        vacancy.setUrl(element.select("a[data-qa=vacancy-serp__vacancy-title]").attr("href"));
                        vacancy.setCity(element.select("[data-qa=\"vacancy-serp__vacancy-address\"]").first().text());
                        vacancy.setCompanyName(element.select("[data-qa=\"vacancy-serp__vacancy-employer\"").first().text());
                        vacancy.setSiteName("http://hh.ua/");
                        vacancy.setTitle(element.select("[data-qa=\"vacancy-serp__vacancy-title\"]").first().text());
                        String salary = element.select("[data-qa=\"vacancy-serp__vacancy-compensation\"]").text();
                        vacancy.setSalary(salary != null ? salary : "");
                        vacancies.add(vacancy);
                    }
                }else break;
            }while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vacancies;
    }
}

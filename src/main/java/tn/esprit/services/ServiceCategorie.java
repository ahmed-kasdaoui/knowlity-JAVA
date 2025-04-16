package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Categorie;
import tn.esprit.models.Matiere;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements IService<Categorie> {
    private Connection cnx;

    public ServiceCategorie() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Categorie categorie) {
        String qry = "INSERT INTO `categorie` (`name`, `descrption`, `icone`, `mots_cles`, `public_cible`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, categorie.getName());
            pstm.setString(2, categorie.getDescrption());
            pstm.setString(3, categorie.getIcone());
            pstm.setString(4, categorie.getMotsCles());
            pstm.setString(5, categorie.getPublicCible());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                categorie.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Categorie> getAll() {
        List<Categorie> categories = new ArrayList<>();
        String qry = "SELECT * FROM `categorie`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescrption(rs.getString("descrption"));
                c.setIcone(rs.getString("icone"));
                c.setMotsCles(rs.getString("mots_cles"));
                c.setPublicCible(rs.getString("public_cible"));
                categories.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }

    @Override
    public void update(Categorie categorie) {
        String qry = "UPDATE `categorie` SET `name`=?, `descrption`=?, `icone`=?, `mots_cles`=?, `public_cible`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, categorie.getName());
            pstm.setString(2, categorie.getDescrption());
            pstm.setString(3, categorie.getIcone());
            pstm.setString(4, categorie.getMotsCles());
            pstm.setString(5, categorie.getPublicCible());
            pstm.setInt(6, categorie.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Categorie categorie) {
        String qry = "DELETE FROM `categorie` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, categorie.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Categorie getById(int id) {
        Categorie categorie = null;
        String qry = "SELECT * FROM `categorie` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                categorie = new Categorie();
                categorie.setId(rs.getInt("id"));
                categorie.setName(rs.getString("name"));
                categorie.setDescrption(rs.getString("descrption"));
                categorie.setIcone(rs.getString("icone"));
                categorie.setMotsCles(rs.getString("mots_cles"));
                categorie.setPublicCible(rs.getString("public_cible"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categorie;
    }
}
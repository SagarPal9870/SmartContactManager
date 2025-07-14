package in.sp.main.Controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import in.sp.main.Entity.User;
import in.sp.main.Helper.Message;
import in.sp.main.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;


@Controller
public class MyController {

	@Autowired
	private BCryptPasswordEncoder passEncoder;
	
	@Autowired
	private UserRepository userRepository;

	/*
	 * @GetMapping("/test")
	 * 
	 * @ResponseBody public String test() { User user = new User();
	 * user.setName("Sagar"); user.setEmail("sagar@gmail.com");
	 * 
	 * Contact contact = new Contact(); user.getContact().add(contact);
	 * 
	 * userRepository.save(user); return "Woking"; }
	 */

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - smart contact manager");
		return "home";
	}

	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "about - smart contact manager");
		return "about";
	}
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - smart contact manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	
	//-This handler for registering user 
	@RequestMapping(value="/do_register",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value="agreement",defaultValue="false") boolean agreement,
			Model model,HttpSession session) {
		try {
			
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			 
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			user.setPassword(passEncoder.encode(user.getPassword()));
			
			System.out.println("agreement:"+agreement);
			System.out.println("user :"+user);
			
			User result=this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("successfully Registered","alert-success"));
			return "signup";
		}
		catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!", "alert-danger"));
		}
		return "signup";
	}
	
	//----------for login page controller(Custom login page)-----------
	@GetMapping("/signin")
	public String customLogin(Model model) {
			model.addAttribute("title", "Login Page");
			return "login";
	}
	
	
	
	
}

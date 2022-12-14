package com.bz.BSUserService.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bz.BSUserService.Dto.CartDto;
import com.bz.BSUserService.Model.BookModel;
import com.bz.BSUserService.Model.CartModel;
import com.bz.BSUserService.Model.UserModel;
import com.bz.BSUserService.Repository.IBookRepository;
import com.bz.BSUserService.Repository.ICartRepository;
import com.bz.BSUserService.Repository.IUserRepository;
import com.bz.BSUserService.Util.TokenUtil;

@Service

public class CartService implements ICartService {

	@Autowired
	ICartRepository cartRepo;
	@Autowired
	TokenUtil tokenUtil;
	@Autowired
	IUserRepository userRepo;
	@Autowired
	IBookRepository bookRepo;

	@Override
	public CartModel create(CartDto cartDto) {
		Optional<BookModel> book = bookRepo.findById(cartDto.getBookID());
		Optional<UserModel> user = userRepo.findById(cartDto.getUserID());
		if (book.isPresent() && user.isPresent()) {
			if (cartDto.getQuantity() < book.get().getQuantity()) {
				CartModel newCart = new CartModel(cartDto,book.get(),user.get());
				cartRepo.save(newCart);
				book.get().setQuantity(book.get().getQuantity() - cartDto.getQuantity());
				bookRepo.save(book.get());
				return newCart;
			}
		}
		return null;
	}

	@Override
	public CartModel delete(long id) {
		Optional<CartModel> cart = cartRepo.findById(id);
		cartRepo.delete(cart.get());
		return cart.get();
	}

	@Override
	public CartModel updateCart(Long id, CartDto cartDto) {
		Optional<CartModel> cart = cartRepo.findById(id);
		Optional<BookModel> book = bookRepo.findById(cartDto.getBookID());
		Optional<UserModel> user = userRepo.findById(cartDto.getUserID());
		if (book.isPresent() && user.isPresent()) {
			if (cartDto.getQuantity() < book.get().getQuantity()) {
				cart.get().setQuantity(cartDto.getQuantity());
				cart.get().setBook(book.get());
				cartRepo.save(cart.get());
				book.get().setQuantity(book.get().getQuantity() - (cartDto.getQuantity() - cart.get().getQuantity()));
				bookRepo.save(book.get());
				return cart.get();
			}
		}
		return null;
	}


	@Override
	public List<CartModel> gerCartForUser(long id) {
//		Long id = tokenUtil.decodeToken(token);
		Optional<UserModel> user = userRepo.findById(id);
		List<CartModel> cartList = cartRepo.findAllByUserId(id);
		return cartList;
	}

	@Override
	public CartModel updateQuantity(String token, long cartId, int quantity) {
		Long id = tokenUtil.decodeToken(token);
		Optional<UserModel> user = userRepo.findById(id);
		if (user.isPresent()) {
			Optional<CartModel> cart = cartRepo.findById(cartId);
			cart.get().setQuantity(quantity);
			cart.get().setTotalPrice(quantity*cart.get().getBook().getPrice());
			cartRepo.save(cart.get());
			return cart.get();
		}
		return null;
	}
	
	@Override
	public CartModel updateQuantity(long cartId, int quantity) {
			Optional<CartModel> cart = cartRepo.findById(cartId);
			cart.get().setQuantity(quantity);
			cart.get().setTotalPrice(quantity*cart.get().getBook().getPrice());
			cartRepo.save(cart.get());
			return cart.get();
	}

	@Override
	public int getCount() {
		List list = cartRepo.findAll();
		int count = list.size();
		return count;
	}

}

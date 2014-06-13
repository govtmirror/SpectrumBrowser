/* -*- c++ -*- */
/* 
 * Copyright 2014 <+YOU OR YOUR COMPANY+>.
 * 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street,
 * Boston, MA 02110-1301, USA.
 */


#ifndef INCLUDED_MYBLOCKS_BIN_STATISTICS_FF_H
#define INCLUDED_MYBLOCKS_BIN_STATISTICS_FF_H

#include <myblocks/api.h>
#include <gnuradio/sync_decimator.h>

namespace gr {
  namespace myblocks {

    /*!
     * \brief <+description of block+>
     * \ingroup myblocks
     *
     */
    class MYBLOCKS_API bin_statistics_ff : virtual public gr::sync_decimator
    {
     public:
      typedef boost::shared_ptr<bin_statistics_ff> sptr;

      /*!
       * \brief Return a shared_ptr to a new instance of myblocks::bin_statistics_ff.
       *
       * To avoid accidental use of raw pointers, myblocks::bin_statistics_ff's
       * constructor is in a private implementation
       * class. myblocks::bin_statistics_ff::make is the public interface for
       * creating new instances.
       */
      static sptr make(unsigned int vlen, unsigned int meas_period);
    };

  } // namespace myblocks
} // namespace gr

#endif /* INCLUDED_MYBLOCKS_BIN_STATISTICS_FF_H */

